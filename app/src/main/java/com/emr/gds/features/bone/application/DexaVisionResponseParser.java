package com.emr.gds.features.bone.application;

import com.emr.gds.features.bone.domain.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DexaVisionResponseParser {

    private static final Logger logger = LoggerFactory.getLogger(DexaVisionResponseParser.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

    public static DexaReport parse(String aiResponseText) {
        if (aiResponseText == null || aiResponseText.isBlank()) {
            throw new DexaVisionParseException("AI 응답이 비어있습니다.");
        }

        String jsonText = stripCodeFences(aiResponseText);
        JsonNode root;

        try {
            root = mapper.readTree(jsonText);
        } catch (Exception e) {
            logger.error("JSON 파싱 실패: {}", aiResponseText);
            throw new DexaVisionParseException("AI 응답을 JSON으로 파싱할 수 없습니다: " + e.getMessage(), e);
        }

        if (root == null || !root.isObject()) {
            throw new DexaVisionParseException("AI 응답이 JSON 객체가 아닙니다.");
        }

        DexaReport report = new DexaReport();

        // Patient info
        report.setPatientName(textOrNull(root, "patientName"));
        report.setPatientId(textOrNull(root, "patientId"));
        report.setBirthDate(dateOrNull(root, "birthDate"));
        report.setAge(intOrNull(root, "age"));
        report.setSex(sexOrNull(root, "sex"));
        report.setHeightCm(doubleOrNull(root, "heightCm"));
        report.setWeightKg(doubleOrNull(root, "weightKg"));
        report.setExamDate(dateOrNull(root, "examDate"));
        report.setAnalysisDate(dateOrNull(root, "analysisDate"));

        // Spine measurements
        List<DexaMeasurement> spineMeasurements = arrayOrEmpty(root, "spine", SkeletalSite.SPINE);
        report.setSpineMeasurements(spineMeasurements);

        // Femur measurements
        List<DexaMeasurement> femurMeasurements = arrayOrEmpty(root, "femur", SkeletalSite.FEMUR);
        report.setFemurMeasurements(femurMeasurements);

        if (spineMeasurements.isEmpty() && femurMeasurements.isEmpty()) {
            throw new DexaVisionParseException("추출된 측정치가 없습니다.");
        }

        return report;
    }

    private static String stripCodeFences(String text) {
        String trimmed = text.trim();

        // Remove markdown code fences
        if (trimmed.startsWith("```")) {
            int start = trimmed.indexOf("```");
            int end = trimmed.lastIndexOf("```");
            if (start != end) {
                trimmed = trimmed.substring(start + 3, end).trim();
            } else {
                trimmed = trimmed.substring(3).trim();
            }
        }

        // Remove language identifier after opening fence (e.g., ```json)
        if (trimmed.startsWith("json")) {
            trimmed = trimmed.substring(4).trim();
        }

        // Extract JSON object: find first { and last }
        int jsonStart = trimmed.indexOf('{');
        int jsonEnd = trimmed.lastIndexOf('}');

        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            trimmed = trimmed.substring(jsonStart, jsonEnd + 1);
        }

        return trimmed;
    }

    private static String textOrNull(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull()) {
            return null;
        }
        return field.asText();
    }

    private static LocalDate dateOrNull(JsonNode node, String fieldName) {
        String text = textOrNull(node, fieldName);
        if (text == null) {
            return null;
        }
        try {
            return LocalDate.parse(text, DATE_FORMATTER);
        } catch (Exception e) {
            logger.warn("날짜 파싱 실패: {} = {}", fieldName, text);
            return null;
        }
    }

    private static Integer intOrNull(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull()) {
            return null;
        }
        if (field.isNumber()) {
            return field.asInt();
        }
        try {
            return Integer.parseInt(field.asText());
        } catch (Exception e) {
            logger.warn("정수 파싱 실패: {} = {}", fieldName, field.asText());
            return null;
        }
    }

    private static Double doubleOrNull(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull()) {
            return null;
        }
        if (field.isNumber()) {
            return field.asDouble();
        }
        try {
            return Double.parseDouble(field.asText());
        } catch (Exception e) {
            logger.warn("실수 파싱 실패: {} = {}", fieldName, field.asText());
            return null;
        }
    }

    private static Sex sexOrNull(JsonNode node, String fieldName) {
        String text = textOrNull(node, fieldName);
        if (text == null) {
            return null;
        }
        try {
            return Sex.valueOf(text.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("성별 파싱 실패: {} = {}", fieldName, text);
            return Sex.UNKNOWN;
        }
    }

    private static List<DexaMeasurement> arrayOrEmpty(JsonNode node, String fieldName, SkeletalSite site) {
        List<DexaMeasurement> result = new ArrayList<>();
        JsonNode array = node.get(fieldName);

        if (array == null || !array.isArray()) {
            return result;
        }

        for (JsonNode item : array) {
            DexaMeasurement measurement = toMeasurement(item, site);
            if (measurement != null) {
                result.add(measurement);
            }
        }

        return result;
    }

    private static DexaMeasurement toMeasurement(JsonNode node, SkeletalSite site) {
        if (node == null || !node.isObject()) {
            return null;
        }

        String region = textOrNull(node, "region");
        Double bmd = doubleOrNull(node, "bmd");
        Double tScore = doubleOrNull(node, "tScore");
        Double zScore = doubleOrNull(node, "zScore");

        if (region == null) {
            logger.warn("부위명이 없는 측정치는 건너뜁니다.");
            return null;
        }

        return new DexaMeasurement(region, site, bmd, tScore, zScore);
    }
}
