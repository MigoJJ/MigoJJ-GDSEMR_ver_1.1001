package com.emr.gds.repository.sqlite;

import com.emr.gds.core.config.RuntimeEnvironment;
import java.nio.file.Path;

/**
 * Resolves standardized SQLite database paths.
 */
public final class SqliteDatabasePaths {
    private SqliteDatabasePaths() {
    }

    public static Path resolveDbPath(String fileName) {
        return RuntimeEnvironment.resolveDatabasePath(fileName);
    }
}
