package com.emr.gds.core.config;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Manages runtime environment settings and standardized directory structures.
 */
public class RuntimeEnvironment {

    private static final String APP_DIR_NAME = ".gdsemr";
    private static final String DB_SUBDIR = "db";
    private static final String LOG_SUBDIR = "logs";

    /**
     * Returns the base directory for application data.
     * Defaults to ~/.gdsemr or can be overridden by GDSEMR_HOME environment variable.
     */
    public static Path getBaseDataDirectory() {
        String envHome = System.getenv("GDSEMR_HOME");
        if (envHome != null && !envHome.isBlank()) {
            return Paths.get(envHome);
        }
        return Paths.get(System.getProperty("user.home"), APP_DIR_NAME);
    }

    /**
     * Returns the standardized path for database files.
     */
    public static Path getDatabaseDirectory() {
        return getBaseDataDirectory().resolve(DB_SUBDIR);
    }

    /**
     * Returns the standardized path for log files.
     */
    public static Path getLogDirectory() {
        return getBaseDataDirectory().resolve(LOG_SUBDIR);
    }

    /**
     * Resolves a database file path.
     */
    public static Path resolveDatabasePath(String fileName) {
        return getDatabaseDirectory().resolve(fileName);
    }
}
