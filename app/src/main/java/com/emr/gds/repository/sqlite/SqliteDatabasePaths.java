package com.emr.gds.repository.sqlite;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resolves repository-local SQLite database paths.
 */
public final class SqliteDatabasePaths {
    private SqliteDatabasePaths() {
    }

    public static Path resolveDbPath(String fileName) {
        Path p = Paths.get("").toAbsolutePath();
        while (p != null && !Files.exists(p.resolve("gradlew")) && !Files.exists(p.resolve(".git"))) {
            p = p.getParent();
        }
        return (p != null) ? p.resolve("app").resolve("db").resolve(fileName)
                : Paths.get("app").resolve("db").resolve(fileName);
    }
}
