package com.emr.gds.context;

import com.emr.gds.core.config.RuntimeEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Ensures initial data and directory structures are in place.
 */
public class DataInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    public static void initialize() {
        try {
            ensureDirectories();
            ensureInitialDatabases();
        } catch (IOException e) {
            logger.error("Failed to initialize data", e);
        }
    }

    private static void ensureDirectories() throws IOException {
        Path dbDir = RuntimeEnvironment.getDatabaseDirectory();
        if (!Files.exists(dbDir)) {
            logger.info("Creating database directory: {}", dbDir);
            Files.createDirectories(dbDir);
        }
        
        Path logDir = RuntimeEnvironment.getLogDirectory();
        if (!Files.exists(logDir)) {
            logger.info("Creating log directory: {}", logDir);
            Files.createDirectories(logDir);
        }
    }

    private static void ensureInitialDatabases() throws IOException {
        String[] dbFiles = {"abbreviations.db", "history.db", "references.db", "emr_templates.db", "med_data.db"};
        
        // In a real app, we might copy from resources. 
        // For this prototype, we'll try to find the template DBs in the repo if they don't exist in user home.
        Path repoRoot = findRepoRoot();
        if (repoRoot == null) {
            logger.warn("Could not find repo root, skipping initial DB copy.");
            return;
        }

        Path sourceDir = repoRoot.resolve("app").resolve("db");
        Path targetDir = RuntimeEnvironment.getDatabaseDirectory();

        for (String dbFile : dbFiles) {
            Path targetPath = targetDir.resolve(dbFile);
            if (!Files.exists(targetPath)) {
                Path sourcePath = sourceDir.resolve(dbFile);
                if (Files.exists(sourcePath)) {
                    logger.info("Copying initial database: {} -> {}", sourcePath, targetPath);
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    logger.warn("Source database not found: {}", sourcePath);
                }
            }
        }
    }

    private static Path findRepoRoot() {
        Path p = Paths.get("").toAbsolutePath();
        while (p != null && !Files.exists(p.resolve("gradlew")) && !Files.exists(p.resolve(".git"))) {
            p = p.getParent();
        }
        return p;
    }
}
