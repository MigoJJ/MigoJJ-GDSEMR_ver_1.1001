package com.emr.gds.features.bone.domain;

public enum ReferenceRegion {
    LUMBAR_SPINE_L1_L4,
    FEMORAL_NECK,
    TOTAL_HIP,
    OTHER;

    public static ReferenceRegion match(String regionName) {
        if (regionName == null || regionName.isBlank()) {
            return OTHER;
        }
        String normalized = regionName.trim().toUpperCase();

        if (normalized.contains("L1") && normalized.contains("L4") ||
            normalized.equals("LUMBAR") || normalized.equals("L1-L4")) {
            return LUMBAR_SPINE_L1_L4;
        }
        if (normalized.contains("NECK") || normalized.equals("FEMORAL NECK") || normalized.equals("FN")) {
            return FEMORAL_NECK;
        }
        if (normalized.contains("TOTAL") && normalized.contains("HIP") || normalized.equals("TH")) {
            return TOTAL_HIP;
        }
        return OTHER;
    }
}
