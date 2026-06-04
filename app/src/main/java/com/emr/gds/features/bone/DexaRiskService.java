package com.emr.gds.features.bone;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * DEXA 골밀도 진단 로직.
 * WHO 기준 T-Score 분류: ≥ -1.0 정상, -1.0~-2.5 골감소증, ≤ -2.5 골다공증.
 */
public class DexaRiskService {

    /**
     * T-Score 또는 Z-Score 기반 골밀도 진단명을 반환합니다.
     *
     * @param score    측정값 (음수 클수록 골밀도 낮음)
     * @param isTScore true = T-Score, false = Z-Score
     * @param hasFracture 취약 골절 이력 여부
     */
    public String diagnose(double score, boolean isTScore, boolean hasFracture) {
        if (isTScore) {
            if (score <= -2.5) return hasFracture ? "Severe Osteoporosis" : "Osteoporosis";
            if (score <  -1.0) return "Osteopenia";
            return "Normal Bone Density";
        } else {
            return score <= -2.0 ? "Below expected range for age" : "Within expected range for age";
        }
    }

    /**
     * 완전한 DEXA 보고서 텍스트를 생성합니다.
     */
    public String generateReport(double score, boolean isTScore, int age, String gender,
                                  boolean hasFracture, boolean isMenopausal,
                                  boolean onHrt, boolean hasTah, boolean hasStones) {

        String scoreType = isTScore ? "T-Score" : "Z-Score";
        String diagnosis = diagnose(score, isTScore, hasFracture);
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("< DEXA Report - %s >\n", date));
        sb.append(String.format("Diagnosis: %s (%s: %.1f)\n", diagnosis, scoreType, score));
        sb.append(String.format("Patient: %d-year-old %s\n", age, gender));

        if ("Female".equals(gender)) {
            sb.append(String.format(
                    "Clinical Factors → Menopausal: %s | Fragility Fx: %s | On HRT: %s | TAH: %s | Kidney Stones: %s\n",
                    yn(isMenopausal), yn(hasFracture), yn(onHrt), yn(hasTah), yn(hasStones)));
        } else {
            sb.append(String.format(
                    "Clinical Factors → Fragility Fx: %s | Kidney Stones: %s\n",
                    yn(hasFracture), yn(hasStones)));
        }

        sb.append("\nComment>\n");
        sb.append(String.format("# %s based on %s of %.1f.\n", diagnosis, scoreType, score));
        if (isTScore && score <= -2.5) {
            sb.append("# Consider bisphosphonate, denosumab, or anabolic therapy.\n");
        } else if (isTScore && score < -1.0) {
            sb.append("# Lifestyle modification, calcium + vitamin D, repeat DEXA in 2–3 years.\n");
        }
        return sb.toString();
    }

    private String yn(boolean b) { return b ? "Yes" : "No"; }
}
