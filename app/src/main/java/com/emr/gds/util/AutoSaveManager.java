package com.emr.gds.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.emr.gds.soap.model.PMHEntry; // Assuming PMHEntry is the data to be saved

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AutoSaveManager {
    private static final Logger logger = LoggerFactory.getLogger(AutoSaveManager.class);
    private final Path savePath;
    private final ObjectMapper mapper;
    
    // Default save location in user's home directory
    public AutoSaveManager(String fileName) {
        this.savePath = Paths.get(System.getProperty("user.home"), ".emr", fileName);
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print JSON
    }
    
    public void autoSave(List<PMHEntry> entries) {
        try {
            Files.createDirectories(savePath.getParent());
            mapper.writeValue(savePath.toFile(), entries);
            logger.info("Auto-saved PMH entries to: {}", savePath);
        } catch (IOException e) {
            logger.warn("Auto-save failed to {}: {}", savePath, e.getMessage(), e);
        }
    }
    
    public Optional<List<PMHEntry>> restore() {
        if (!Files.exists(savePath)) {
            logger.info("No auto-save file found at: {}", savePath);
            return Optional.empty();
        }
        
        try {
            List<PMHEntry> entries = mapper.readValue(
                savePath.toFile(), 
                new TypeReference<List<PMHEntry>>() {}
            );
            logger.info("Restored PMH entries from: {}", savePath);
            return Optional.of(entries);
        } catch (IOException e) {
            logger.error("Auto-restore failed from {}: {}", savePath, e.getMessage(), e);
            return Optional.empty();
        }
    }
}
