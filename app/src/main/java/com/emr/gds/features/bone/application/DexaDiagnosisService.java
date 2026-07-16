package com.emr.gds.features.bone.application;

import com.emr.gds.features.bone.domain.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DexaDiagnosisService {

    public DexaDiagnosisResult diagnose(DexaReport report, DexaRiskFactors riskFactors) {
        // 1. Hip/vertebral fracture override (최우선)
        if (riskFactors.hipOrVertebralFracture()) {
            DexaMeasurement refMeasurement = report.allMeasurements().stream()
                    .min(Comparator.comparingDouble(m -> m.tScore() != null ? m.tScore() : Double.POSITIVE_INFINITY))
                    .orElse(null);
            String rationale = "고관절 또는 척추 취약골절이 있어 T-score와 무관하게 골다공증으로 진단됩니다.";
            return new DexaDiagnosisResult(DexaDiagnosis.OSTEOPOROSIS_BY_FRACTURE, refMeasurement, ScoreType.T_SCORE, rationale);
        }

        // 2. Determine scoreType based on age/sex/postmenopausal status
        ScoreType scoreType = determineScoreType(report, riskFactors);

        // 3. Collect reference measurements (only L1-L4, Neck, Total Hip)
        List<DexaMeasurement> referenceMeasurements = collectReferenceMeasurements(report);

        if (referenceMeasurements.isEmpty()) {
            throw new IllegalStateException("진단을 위한 기준 부위(요추 L1-L4, 대퇴경부, 전체 고관절) 측정값이 없습니다.");
        }

        // 4. Apply diagnostic criteria based on scoreType
        if (scoreType == ScoreType.T_SCORE) {
            return diagnoseBySCore(referenceMeasurements, riskFactors);
        } else {
            return diagnoseByZScore(referenceMeasurements);
        }
    }

    private ScoreType determineScoreType(DexaReport report, DexaRiskFactors riskFactors) {
        Integer age = report.getAge();
        Sex sex = report.getSex();

        // Children: age < 18 → Z-score
        if (age != null && age < 18) {
            return ScoreType.Z_SCORE;
        }

        // Premenopausal women → Z-score
        if (sex == Sex.FEMALE && !riskFactors.postmenopausal()) {
            return ScoreType.Z_SCORE;
        }

        // Men < 50 → Z-score
        if (sex == Sex.MALE && age != null && age < 50) {
            return ScoreType.Z_SCORE;
        }

        // Others (postmenopausal women, men >= 50) → T-score
        return ScoreType.T_SCORE;
    }

    private List<DexaMeasurement> collectReferenceMeasurements(DexaReport report) {
        List<DexaMeasurement> references = new ArrayList<>();

        for (DexaMeasurement m : report.allMeasurements()) {
            ReferenceRegion region = ReferenceRegion.match(m.region());
            if (region == ReferenceRegion.LUMBAR_SPINE_L1_L4 ||
                region == ReferenceRegion.FEMORAL_NECK ||
                region == ReferenceRegion.TOTAL_HIP) {
                references.add(m);
            }
        }

        return references;
    }

    private DexaDiagnosisResult diagnoseBySCore(List<DexaMeasurement> references, DexaRiskFactors riskFactors) {
        // Find measurement with lowest T-score
        DexaMeasurement lowestMeasurement = references.stream()
                .filter(m -> m.tScore() != null)
                .min(Comparator.comparingDouble(DexaMeasurement::tScore))
                .orElse(null);

        if (lowestMeasurement == null) {
            throw new IllegalStateException("T-score 값이 없는 측정만 존재합니다.");
        }

        double tScore = lowestMeasurement.tScore();
        DexaDiagnosis diagnosis;
        String rationale;

        if (tScore <= -2.5) {
            if (riskFactors.fragilityFracture()) {
                diagnosis = DexaDiagnosis.SEVERE_OSTEOPOROSIS;
                rationale = String.format("T-score %.1f (≤ -2.5) + 취약골절 = 심한 골다공증", tScore);
            } else {
                diagnosis = DexaDiagnosis.OSTEOPOROSIS;
                rationale = String.format("T-score %.1f (≤ -2.5) = 골다공증", tScore);
            }
        } else if (tScore > -2.5 && tScore < -1.0) {
            diagnosis = DexaDiagnosis.OSTEOPENIA;
            rationale = String.format("T-score %.1f (-2.5 < T < -1.0) = 골감소증", tScore);
        } else {
            diagnosis = DexaDiagnosis.NORMAL;
            rationale = String.format("T-score %.1f (≥ -1.0) = 정상", tScore);
        }

        return new DexaDiagnosisResult(diagnosis, lowestMeasurement, ScoreType.T_SCORE, rationale);
    }

    private DexaDiagnosisResult diagnoseByZScore(List<DexaMeasurement> references) {
        // Find measurement with lowest Z-score
        DexaMeasurement lowestMeasurement = references.stream()
                .filter(m -> m.zScore() != null)
                .min(Comparator.comparingDouble(DexaMeasurement::zScore))
                .orElse(null);

        if (lowestMeasurement == null) {
            throw new IllegalStateException("Z-score 값이 없는 측정만 존재합니다.");
        }

        double zScore = lowestMeasurement.zScore();
        DexaDiagnosis diagnosis;
        String rationale;

        if (zScore <= -2.0) {
            diagnosis = DexaDiagnosis.BELOW_EXPECTED_RANGE_FOR_AGE;
            rationale = String.format("Z-score %.1f (≤ -2.0) = 연령대비 저하", zScore);
        } else {
            diagnosis = DexaDiagnosis.WITHIN_EXPECTED_RANGE_FOR_AGE;
            rationale = String.format("Z-score %.1f (> -2.0) = 연령대비 정상범위", zScore);
        }

        return new DexaDiagnosisResult(diagnosis, lowestMeasurement, ScoreType.Z_SCORE, rationale);
    }
}
