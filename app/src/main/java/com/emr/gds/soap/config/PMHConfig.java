package com.emr.gds.soap.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Collections;

public class PMHConfig {
    private List<CategoryConfig> categories;
    private int numColumns;
    // UITheme theme; // This will be added later when ThemeManager is implemented

    // Constructors
    public PMHConfig() {
        this.categories = Collections.emptyList();
        this.numColumns = 4; // Default value
    }

    // Getters
    public List<CategoryConfig> getCategories() {
        return categories;
    }

    public int getNumColumns() {
        return numColumns;
    }

    // Setters (required for JSON deserialization)
    public void setCategories(List<CategoryConfig> categories) {
        this.categories = categories;
    }

    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
    }

    /**
     * Loads PMHConfig from a JSON file.
     * The file is expected to be in the classpath (e.g., in resources folder).
     * @param path The path to the JSON configuration file.
     * @return PMHConfig instance.
     * @throws IOException if an error occurs during file reading or JSON parsing.
     */
    public static PMHConfig load(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = PMHConfig.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new IOException("Configuration file not found: " + path);
            }
            return mapper.readValue(is, PMHConfig.class);
        }
    }
}
