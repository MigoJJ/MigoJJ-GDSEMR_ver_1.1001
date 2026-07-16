package com.emr.gds.infrastructure.ai;

import key.emr.ittia.service.AiService;
import key.emr.ittia.service.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * High-level service for AI-assisted medical documentation.
 * Bridges the EMR app with the AI gateway module.
 */
public class AiAssistantService {
    private static final Logger logger = LoggerFactory.getLogger(AiAssistantService.class);
    private final AiService aiService;

    public AiAssistantService() {
        // Default to Gemini, uses environment variable GEMINI_API_KEY internally
        this.aiService = new GeminiService();
    }

    /**
     * Generates a clinical summary or documentation draft based on findings.
     */
    public CompletableFuture<String> generateClinicalDraft(String prompt) {
        logger.info("Requesting AI clinical draft...");
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Using a default medical model
                String model = "gemini-1.5-flash";
                return aiService.generateResponse(model, prompt, Collections.emptyList());
            } catch (Exception e) {
                logger.error("AI generation failed", e);
                return "Error: Unable to generate AI draft. Please check your API key and connection.";
            }
        });
    }

    /**
     * Generates a clinical summary based on findings and images.
     * Used for image-based AI analysis (e.g., DEXA image parsing).
     */
    public CompletableFuture<String> generateClinicalDraft(String prompt, List<Path> images) {
        logger.info("Requesting AI clinical draft with {} images", images.size());
        return CompletableFuture.supplyAsync(() -> {
            try {
                String model = "gemini-1.5-flash";
                return aiService.generateResponse(model, prompt, images);
            } catch (Exception e) {
                logger.error("AI image-based generation failed", e);
                throw new CompletionException("AI 이미지 판독 요청이 실패했습니다: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Builds a specific prompt for thyroid documentation.
     */
    public String buildThyroidPrompt(String findings) {
        return """
            You are a thyroid specialist assistant. 
            Based on the following clinical findings, generate a professional, structured medical summary in English.
            Include Assessment and Plan sections. Keep it concise and clinical.
            
            Findings:
            %s
            """.formatted(findings);
    }
}
