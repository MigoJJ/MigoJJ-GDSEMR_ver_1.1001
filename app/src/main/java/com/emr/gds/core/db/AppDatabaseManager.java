package com.emr.gds.core.db;

import com.emr.gds.core.config.RuntimeEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centralized manager for application SQLite connections.
 *
 * Two categories of DB are managed here:
 *  - User-data DBs  (abbreviations, history, references, plan_history, prolist):
 *    shared persistent connections, located in ~/.gdsemr/db/ (or $GDSEMR_HOME/db/).
 *  - App reference-data DBs (medication, clinicalLab, templates, kcd):
 *    short-lived connections opened per call; use resolveAppDbPath() for the file path.
 *
 * Call closeAll() once on application shutdown.
 */
public class AppDatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(AppDatabaseManager.class);
    private static final AppDatabaseManager INSTANCE = new AppDatabaseManager();

    // ── User-data shared connections ──────────────────────────────────────
    private Connection abbreviationConnection;
    private Connection historyConnection;
    private Connection referenceConnection;
    private Connection planHistoryConnection;
    private Connection problemConnection;
    private Connection dexaReportsConnection;

    private AppDatabaseManager() {}

    public static AppDatabaseManager getInstance() {
        return INSTANCE;
    }

    // ── User-data connection accessors ────────────────────────────────────

    public synchronized Connection getAbbreviationConnection() throws SQLException {
        if (abbreviationConnection == null || abbreviationConnection.isClosed()) {
            abbreviationConnection = openConnection("abbreviations.db");
        }
        return abbreviationConnection;
    }

    public synchronized Connection getHistoryConnection() throws SQLException {
        if (historyConnection == null || historyConnection.isClosed()) {
            historyConnection = openConnection("history.db");
        }
        return historyConnection;
    }

    public synchronized Connection getReferenceConnection() throws SQLException {
        if (referenceConnection == null || referenceConnection.isClosed()) {
            referenceConnection = openConnection("references.db");
        }
        return referenceConnection;
    }

    public synchronized Connection getPlanHistoryConnection() throws SQLException {
        if (planHistoryConnection == null || planHistoryConnection.isClosed()) {
            planHistoryConnection = openConnection("plan_history.db");
        }
        return planHistoryConnection;
    }

    public synchronized Connection getProblemConnection() throws SQLException {
        if (problemConnection == null || problemConnection.isClosed()) {
            problemConnection = openConnection("prolist.db");
        }
        return problemConnection;
    }

    public synchronized Connection getDexaReportsConnection() throws SQLException {
        if (dexaReportsConnection == null || dexaReportsConnection.isClosed()) {
            dexaReportsConnection = openConnection("dexa_reports.db");
        }
        return dexaReportsConnection;
    }

    // ── App reference-data path resolver ─────────────────────────────────

    /**
     * Resolves the path of an app reference-data DB (med_data, ClinicalLabItems, etc.).
     * Searches upward from the working directory until it finds the project root
     * (identified by gradlew), then returns app/db/<dbFileName>.
     * Falls back to app/db/<dbFileName> relative to CWD if root is not found.
     */
    public static Path resolveAppDbPath(String dbFileName) {
        Path root = findProjectRoot();
        if (root != null) {
            // 1순위: app/db/ (편집 가능한 참조 데이터)
            Path appDb = root.resolve("app").resolve("db").resolve(dbFileName);
            if (Files.exists(appDb)) return appDb;
            // 2순위: app/src/main/resources/database/ (번들 리소스 DB, e.g. KCD)
            Path resourceDb = root.resolve("app").resolve("src").resolve("main")
                                  .resolve("resources").resolve("database").resolve(dbFileName);
            if (Files.exists(resourceDb)) return resourceDb;
            // 3순위: app/build/resources/main/database/ (빌드 후 복사본)
            Path buildDb = root.resolve("app").resolve("build").resolve("resources")
                               .resolve("main").resolve("database").resolve(dbFileName);
            if (Files.exists(buildDb)) return buildDb;
            return appDb; // 기본값
        }
        return Paths.get("app", "db", dbFileName);
    }

    private static Path findProjectRoot() {
        Path p = Paths.get("").toAbsolutePath();
        while (p != null && !Files.exists(p.resolve("gradlew"))) {
            p = p.getParent();
        }
        return p;
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────

    /** Closes all managed connections. Call once on application shutdown. */
    public synchronized void closeAll() {
        logger.info("모든 DB 커넥션을 종료합니다.");
        closeQuietly(abbreviationConnection);  abbreviationConnection  = null;
        closeQuietly(historyConnection);       historyConnection       = null;
        closeQuietly(referenceConnection);     referenceConnection     = null;
        closeQuietly(planHistoryConnection);   planHistoryConnection   = null;
        closeQuietly(problemConnection);       problemConnection       = null;
        closeQuietly(dexaReportsConnection);   dexaReportsConnection   = null;
    }

    // ── Internal helpers ──────────────────────────────────────────────────

    private Connection openConnection(String dbFileName) throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite 드라이버를 찾을 수 없음", e);
        }
        Path dbPath = RuntimeEnvironment.resolveDatabasePath(dbFileName);
        try {
            if (dbPath.getParent() != null) {
                Files.createDirectories(dbPath.getParent());
            }
        } catch (Exception e) {
            throw new SQLException("DB 디렉토리 생성 실패: " + dbPath.getParent(), e);
        }
        String url = "jdbc:sqlite:" + dbPath.toAbsolutePath();
        logger.info("DB 커넥션 열기: {}", url);
        return DriverManager.getConnection(url);
    }

    private void closeQuietly(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException e) {
                logger.warn("커넥션 종료 중 오류", e);
            }
        }
    }
}
