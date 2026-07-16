package com.emr.gds.features.bone.application;

public class DexaVisionPromptBuilder {

    public static String buildExtractionPrompt() {
        return """
            You are analyzing a DEXA (Dual-Energy X-ray Absorptiometry) bone density report image.
            Extract all numerical data and patient information from the report.

            Return your response as a SINGLE JSON OBJECT (no markdown code fences, no additional text before or after).

            JSON Schema:
            {
              "patientName": "string or null",
              "patientId": "string or null",
              "birthDate": "yyyy-MM-dd or null",
              "age": "integer or null",
              "sex": "MALE|FEMALE|UNKNOWN or null",
              "heightCm": "number or null",
              "weightKg": "number or null",
              "examDate": "yyyy-MM-dd or null",
              "analysisDate": "yyyy-MM-dd or null",
              "spine": [
                {
                  "region": "string (e.g. 'L1', 'L2', 'L3', 'L4', 'L1-L2', 'L1-L3', 'L1-L4', 'L2-L3', 'L2-L4', 'L3-L4')",
                  "bmd": "number or null",
                  "tScore": "number or null",
                  "zScore": "number or null"
                }
              ],
              "femur": [
                {
                  "region": "string (e.g. 'Neck', 'Wards', 'Troch', 'Shaft', 'Total')",
                  "bmd": "number or null",
                  "tScore": "number or null",
                  "zScore": "number or null"
                }
              ]
            }

            Rules:
            - Extract EXACTLY as they appear on the report (do not round or calculate)
            - If a value is unreadable or missing, use null
            - spine: include all lumbar spine measurements found
            - femur: include all femur measurements found (Neck, Wards, Troch, Shaft, Total)
            - Return valid JSON only, nothing else
            """;
    }
}
