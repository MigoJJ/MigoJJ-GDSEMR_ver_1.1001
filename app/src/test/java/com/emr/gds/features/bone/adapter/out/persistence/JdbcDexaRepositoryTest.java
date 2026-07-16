package com.emr.gds.features.bone.adapter.out.persistence;

import com.emr.gds.features.bone.domain.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JdbcDexaRepository 통합 테스트")
class JdbcDexaRepositoryTest {

    private Connection testConnection;
    private JdbcDexaRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        // In-memory SQLite for testing
        testConnection = DriverManager.getConnection("jdbc:sqlite::memory:");
        repository = new JdbcDexaRepository(testConnection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }

    @Test
    @DisplayName("DEXA 보고서 저장 및 조회")
    void testSaveAndFindById() {
        DexaReport report = createTestReport();
        repository.save(report);

        // The repository should have assigned an ID during save
        List<DexaReport> recent = repository.findRecent(1);
        assertThat(recent).hasSize(1);

        DexaReport retrieved = recent.get(0);
        assertThat(retrieved.getPatientName()).isEqualTo("John Doe");
        assertThat(retrieved.getPatientId()).isEqualTo("P12345");
        assertThat(retrieved.getAge()).isEqualTo(60);
        assertThat(retrieved.getSex()).isEqualTo(Sex.FEMALE);
    }

    @Test
    @DisplayName("척추 및 대퇴부 측정치 저장")
    void testSaveMeasurements() {
        DexaReport report = createTestReport();
        repository.save(report);

        List<DexaReport> recent = repository.findRecent(1);
        DexaReport retrieved = recent.get(0);

        assertThat(retrieved.getSpineMeasurements()).hasSize(2);
        assertThat(retrieved.getFemurMeasurements()).hasSize(2);

        // Verify spine measurements
        assertThat(retrieved.getSpineMeasurements().get(0).region()).isEqualTo("L1-L4");
        assertThat(retrieved.getSpineMeasurements().get(0).tScore()).isEqualTo(-1.5);

        // Verify femur measurements
        assertThat(retrieved.getFemurMeasurements().get(0).region()).isEqualTo("Neck");
        assertThat(retrieved.getFemurMeasurements().get(0).tScore()).isEqualTo(-2.8);
    }

    @Test
    @DisplayName("최근 보고서 조회 (limit)")
    void testFindRecent() {
        // Save 3 reports
        DexaReport report1 = createTestReport();
        report1.setPatientName("Patient 1");
        repository.save(report1);

        DexaReport report2 = createTestReport();
        report2.setPatientName("Patient 2");
        repository.save(report2);

        DexaReport report3 = createTestReport();
        report3.setPatientName("Patient 3");
        repository.save(report3);

        // Find recent limit 2
        List<DexaReport> recent = repository.findRecent(2);
        assertThat(recent).hasSize(2);

        // Should be in reverse order (most recent first)
        assertThat(recent.get(0).getPatientName()).isEqualTo("Patient 3");
        assertThat(recent.get(1).getPatientName()).isEqualTo("Patient 2");
    }

    @Test
    @DisplayName("진단 결과 저장 및 조회")
    void testSaveDiagnosis() {
        DexaReport report = createTestReport();
        report.setDiagnosis(DexaDiagnosis.OSTEOPOROSIS);
        report.setDiagnosisRationale("T-score <= -2.5");

        repository.save(report);

        List<DexaReport> recent = repository.findRecent(1);
        DexaReport retrieved = recent.get(0);

        assertThat(retrieved.getDiagnosis()).isEqualTo(DexaDiagnosis.OSTEOPOROSIS);
        assertThat(retrieved.getDiagnosisRationale()).isEqualTo("T-score <= -2.5");
    }

    @Test
    @DisplayName("Z-score 값 저장")
    void testSaveZScore() {
        DexaReport report = createTestReport();
        report.getSpineMeasurements().clear();
        report.getSpineMeasurements().add(new DexaMeasurement("L1-L4", SkeletalSite.SPINE, 1.0, null, -1.8));

        repository.save(report);

        List<DexaReport> recent = repository.findRecent(1);
        DexaReport retrieved = recent.get(0);

        assertThat(retrieved.getSpineMeasurements().get(0).zScore()).isEqualTo(-1.8);
        assertThat(retrieved.getSpineMeasurements().get(0).tScore()).isNull();
    }

    @Test
    @DisplayName("빈 측정치 리스트 저장")
    void testSaveEmptyMeasurements() {
        DexaReport report = createTestReport();
        report.getSpineMeasurements().clear();
        report.getFemurMeasurements().clear();

        repository.save(report);

        List<DexaReport> recent = repository.findRecent(1);
        DexaReport retrieved = recent.get(0);

        assertThat(retrieved.getSpineMeasurements()).isEmpty();
        assertThat(retrieved.getFemurMeasurements()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 ID 조회")
    void testFindByIdNotFound() {
        // First save a report to ensure schema is created
        DexaReport report = createTestReport();
        repository.save(report);

        // Now try to find a non-existent ID
        DexaReport result = repository.findById(99999L);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("이미지 경로 저장")
    void testSaveImagePath() {
        DexaReport report = createTestReport();
        report.setSourceImagePath("/home/user/.gdsemr/images/dexa/abc123.jpg");

        repository.save(report);

        List<DexaReport> recent = repository.findRecent(1);
        DexaReport retrieved = recent.get(0);

        assertThat(retrieved.getSourceImagePath()).isEqualTo("/home/user/.gdsemr/images/dexa/abc123.jpg");
    }

    @Test
    @DisplayName("날짜 필드 저장")
    void testSaveDates() {
        DexaReport report = createTestReport();
        LocalDate birthDate = LocalDate.of(1965, 5, 15);
        LocalDate examDate = LocalDate.of(2026, 7, 16);

        report.setBirthDate(birthDate);
        report.setExamDate(examDate);

        repository.save(report);

        List<DexaReport> recent = repository.findRecent(1);
        DexaReport retrieved = recent.get(0);

        assertThat(retrieved.getBirthDate()).isEqualTo(birthDate);
        assertThat(retrieved.getExamDate()).isEqualTo(examDate);
    }

    // ── Test Helpers ──────────────────────────────────────────────────

    private DexaReport createTestReport() {
        DexaReport report = new DexaReport();
        report.setPatientName("John Doe");
        report.setPatientId("P12345");
        report.setAge(60);
        report.setSex(Sex.FEMALE);
        report.setHeightCm(165.0);
        report.setWeightKg(70.0);
        report.setBirthDate(LocalDate.of(1965, 5, 15));
        report.setExamDate(LocalDate.of(2026, 7, 16));
        report.setAnalysisDate(LocalDate.of(2026, 7, 16));

        report.getSpineMeasurements().add(new DexaMeasurement("L1-L4", SkeletalSite.SPINE, 1.0, -1.5, null));
        report.getSpineMeasurements().add(new DexaMeasurement("L1", SkeletalSite.SPINE, 1.05, -1.2, null));

        report.getFemurMeasurements().add(new DexaMeasurement("Neck", SkeletalSite.FEMUR, 0.8, -2.8, null));
        report.getFemurMeasurements().add(new DexaMeasurement("Total Hip", SkeletalSite.FEMUR, 1.0, -1.1, null));

        return report;
    }
}
