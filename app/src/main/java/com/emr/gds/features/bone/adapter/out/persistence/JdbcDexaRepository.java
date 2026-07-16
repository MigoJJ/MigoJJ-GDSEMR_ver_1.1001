package com.emr.gds.features.bone.adapter.out.persistence;

import com.emr.gds.core.db.AppDatabaseManager;
import com.emr.gds.features.bone.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JdbcDexaRepository implements DexaRepository {

    private static final Logger logger = LoggerFactory.getLogger(JdbcDexaRepository.class);

    private final Connection testConnection;

    public JdbcDexaRepository() {
        this(null);
    }

    JdbcDexaRepository(Connection testConnection) {
        this.testConnection = testConnection;
    }

    private Connection conn() throws SQLException {
        return testConnection != null ? testConnection : AppDatabaseManager.getInstance().getDexaReportsConnection();
    }

    @Override
    public void save(DexaReport report) {
        Connection conn = null;
        try {
            conn = conn();
            boolean autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                ensureSchema(conn);

                // Insert header
                long reportId = insertHeader(conn, report);

                // Insert spine measurements
                for (DexaMeasurement m : report.getSpineMeasurements()) {
                    insertMeasurement(conn, reportId, SkeletalSite.SPINE, m);
                }

                // Insert femur measurements
                for (DexaMeasurement m : report.getFemurMeasurements()) {
                    insertMeasurement(conn, reportId, SkeletalSite.FEMUR, m);
                }

                conn.commit();
                logger.info("DEXA 보고서 저장 완료: reportId={}", reportId);

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommitOriginal);
            }

        } catch (SQLException e) {
            logger.error("DEXA 보고서 저장 중 오류", e);
            throw new DexaPersistenceException("DEXA 보고서 저장 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public List<DexaReport> findRecent(int limit) {
        List<DexaReport> reports = new ArrayList<>();
        String sql = "SELECT id, patient_name, patient_id, birth_date, age, sex, height_cm, weight_kg, " +
                     "exam_date, analysis_date, source_image_path, diagnosis, diagnosis_rationale, created_at " +
                     "FROM dexa_report ORDER BY created_at DESC, id DESC LIMIT ?";

        try {
            Connection conn = conn();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, limit);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        DexaReport report = mapReportRow(rs);
                        // Load measurements
                        loadMeasurements(conn, report);
                        reports.add(report);
                    }
                }
            }

        } catch (SQLException e) {
            logger.error("최근 DEXA 보고서 조회 중 오류", e);
            throw new DexaPersistenceException("조회 실패: " + e.getMessage(), e);
        }

        return reports;
    }

    @Override
    public DexaReport findById(long id) {
        String sql = "SELECT id, patient_name, patient_id, birth_date, age, sex, height_cm, weight_kg, " +
                     "exam_date, analysis_date, source_image_path, diagnosis, diagnosis_rationale, created_at " +
                     "FROM dexa_report WHERE id = ?";

        try {
            Connection conn = conn();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setLong(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        DexaReport report = mapReportRow(rs);
                        loadMeasurements(conn, report);
                        return report;
                    }
                }
            }

        } catch (SQLException e) {
            logger.error("ID {}인 DEXA 보고서 조회 중 오류", id, e);
            throw new DexaPersistenceException("ID " + id + " 조회 실패: " + e.getMessage(), e);
        }

        return null;
    }

    // ── Private helpers ──────────────────────────────────────────────────

    private void ensureSchema(Connection conn) throws SQLException {
        String createHeaderTable = """
            CREATE TABLE IF NOT EXISTS dexa_report (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                patient_name TEXT,
                patient_id TEXT,
                birth_date DATE,
                age INTEGER,
                sex TEXT,
                height_cm REAL,
                weight_kg REAL,
                exam_date DATE,
                analysis_date DATE,
                source_image_path TEXT,
                diagnosis TEXT,
                diagnosis_rationale TEXT,
                created_at DATE
            )
            """;

        String createMeasurementTable = """
            CREATE TABLE IF NOT EXISTS dexa_measurement (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                report_id INTEGER NOT NULL,
                site TEXT NOT NULL,
                region TEXT,
                bmd REAL,
                t_score REAL,
                z_score REAL,
                FOREIGN KEY (report_id) REFERENCES dexa_report(id)
            )
            """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createHeaderTable);
            stmt.execute(createMeasurementTable);
        }
    }

    private long insertHeader(Connection conn, DexaReport report) throws SQLException {
        String sql = """
            INSERT INTO dexa_report (patient_name, patient_id, birth_date, age, sex, height_cm, weight_kg,
                                     exam_date, analysis_date, source_image_path, diagnosis, diagnosis_rationale, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, report.getPatientName());
            stmt.setString(2, report.getPatientId());
            stmt.setObject(3, report.getBirthDate());
            stmt.setObject(4, report.getAge());
            stmt.setString(5, report.getSex() != null ? report.getSex().toString() : null);
            stmt.setObject(6, report.getHeightCm());
            stmt.setObject(7, report.getWeightKg());
            stmt.setObject(8, report.getExamDate());
            stmt.setObject(9, report.getAnalysisDate());
            stmt.setString(10, report.getSourceImagePath());
            stmt.setString(11, report.getDiagnosis() != null ? report.getDiagnosis().toString() : null);
            stmt.setString(12, report.getDiagnosisRationale());
            stmt.setObject(13, LocalDate.now());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }

            throw new SQLException("보고서 ID 생성 실패");
        }
    }

    private void insertMeasurement(Connection conn, long reportId, SkeletalSite site, DexaMeasurement measurement)
            throws SQLException {
        String sql = """
            INSERT INTO dexa_measurement (report_id, site, region, bmd, t_score, z_score)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, reportId);
            stmt.setString(2, site.toString());
            stmt.setString(3, measurement.region());
            stmt.setObject(4, measurement.bmd());
            stmt.setObject(5, measurement.tScore());
            stmt.setObject(6, measurement.zScore());

            stmt.executeUpdate();
        }
    }

    private void loadMeasurements(Connection conn, DexaReport report) throws SQLException {
        String sql = "SELECT site, region, bmd, t_score, z_score FROM dexa_measurement WHERE report_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, report.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SkeletalSite site = SkeletalSite.valueOf(rs.getString("site"));
                    DexaMeasurement measurement = new DexaMeasurement(
                            rs.getString("region"),
                            site,
                            (Double) rs.getObject("bmd"),
                            (Double) rs.getObject("t_score"),
                            (Double) rs.getObject("z_score")
                    );

                    if (site == SkeletalSite.SPINE) {
                        report.getSpineMeasurements().add(measurement);
                    } else {
                        report.getFemurMeasurements().add(measurement);
                    }
                }
            }
        }
    }

    private DexaReport mapReportRow(ResultSet rs) throws SQLException {
        DexaReport report = new DexaReport();
        report.setId(rs.getLong("id"));
        report.setPatientName(rs.getString("patient_name"));
        report.setPatientId(rs.getString("patient_id"));
        report.setBirthDate((LocalDate) rs.getObject("birth_date", LocalDate.class));
        report.setAge((Integer) rs.getObject("age"));

        String sexStr = rs.getString("sex");
        if (sexStr != null) {
            try {
                report.setSex(Sex.valueOf(sexStr));
            } catch (IllegalArgumentException e) {
                report.setSex(Sex.UNKNOWN);
            }
        }

        report.setHeightCm((Double) rs.getObject("height_cm"));
        report.setWeightKg((Double) rs.getObject("weight_kg"));
        report.setExamDate((LocalDate) rs.getObject("exam_date", LocalDate.class));
        report.setAnalysisDate((LocalDate) rs.getObject("analysis_date", LocalDate.class));
        report.setSourceImagePath(rs.getString("source_image_path"));

        String diagnosisStr = rs.getString("diagnosis");
        if (diagnosisStr != null) {
            try {
                report.setDiagnosis(DexaDiagnosis.valueOf(diagnosisStr));
            } catch (IllegalArgumentException e) {
                logger.warn("Unknown diagnosis value: {}", diagnosisStr);
            }
        }

        report.setDiagnosisRationale(rs.getString("diagnosis_rationale"));
        report.setCreatedAt((LocalDate) rs.getObject("created_at", LocalDate.class));

        return report;
    }
}
