package com.emr.gds.features.bone.application;

import com.emr.gds.features.bone.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DexaReportService {

    private static final Logger logger = LoggerFactory.getLogger(DexaReportService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final DexaRepository repository;
    private final DexaDiagnosisService diagnosisService;

    public DexaReportService(DexaRepository repository, DexaDiagnosisService diagnosisService) {
        this.repository = repository;
        this.diagnosisService = diagnosisService;
    }

    public void saveReport(DexaReport report) {
        try {
            repository.save(report);
            logger.info("DEXA 보고서 저장 완료: {}", report.getPatientId());
        } catch (DexaPersistenceException e) {
            logger.error("DEXA 보고서 저장 실패", e);
            throw e;
        }
    }

    public String buildReportText(DexaReport report, DexaDiagnosisResult result) {
        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("< BONE DENSITY (DEXA) >\n");
        if (report.getExamDate() != null) {
            sb.append(report.getExamDate().format(DATE_FORMATTER)).append("\n");
        }
        sb.append("\n");

        // Patient info
        if (report.getPatientName() != null || report.getPatientId() != null) {
            sb.append("[환자정보]\n");
            if (report.getPatientName() != null) {
                sb.append("이름: ").append(report.getPatientName()).append("\n");
            }
            if (report.getPatientId() != null) {
                sb.append("ID: ").append(report.getPatientId()).append("\n");
            }
            sb.append("\n");
        }

        // Measurements
        if (!report.getSpineMeasurements().isEmpty()) {
            sb.append("[척추 (Lumbar Spine)]\n");
            for (DexaMeasurement m : report.getSpineMeasurements()) {
                formatMeasurement(sb, m);
            }
            sb.append("\n");
        }

        if (!report.getFemurMeasurements().isEmpty()) {
            sb.append("[대퇴부 (Hip)]\n");
            for (DexaMeasurement m : report.getFemurMeasurements()) {
                formatMeasurement(sb, m);
            }
            sb.append("\n");
        }

        // Diagnosis
        sb.append("[진단]\n");
        sb.append(formatDiagnosis(result.diagnosis())).append("\n");
        if (result.referenceMeasurement() != null) {
            sb.append("기준부위: ").append(result.referenceMeasurement().region());
            if (result.referenceScoreType() == ScoreType.T_SCORE) {
                sb.append(" (T-score: ").append(formatScore(result.referenceMeasurement().tScore())).append(")");
            } else {
                sb.append(" (Z-score: ").append(formatScore(result.referenceMeasurement().zScore())).append(")");
            }
            sb.append("\n");
        }
        if (result.rationale() != null) {
            sb.append("근거: ").append(result.rationale()).append("\n");
        }

        return sb.toString();
    }

    private void formatMeasurement(StringBuilder sb, DexaMeasurement m) {
        sb.append("  ").append(m.region());
        if (m.bmd() != null) {
            sb.append(" BMD: ").append(String.format("%.2f", m.bmd()));
        }
        if (m.tScore() != null) {
            sb.append(" T: ").append(String.format("%.1f", m.tScore()));
        }
        if (m.zScore() != null) {
            sb.append(" Z: ").append(String.format("%.1f", m.zScore()));
        }
        sb.append("\n");
    }

    private String formatScore(Double score) {
        return score != null ? String.format("%.1f", score) : "N/A";
    }

    private String formatDiagnosis(DexaDiagnosis diagnosis) {
        return switch (diagnosis) {
            case NORMAL -> "정상 (Normal)";
            case OSTEOPENIA -> "골감소증 (Osteopenia)";
            case OSTEOPOROSIS -> "골다공증 (Osteoporosis)";
            case SEVERE_OSTEOPOROSIS -> "심한 골다공증 (Severe Osteoporosis)";
            case OSTEOPOROSIS_BY_FRACTURE -> "골다공증 - 취약골절 (Osteoporosis by Fracture)";
            case BELOW_EXPECTED_RANGE_FOR_AGE -> "연령대비 저하 (Below Expected Range)";
            case WITHIN_EXPECTED_RANGE_FOR_AGE -> "연령대비 정상 (Within Expected Range)";
        };
    }
}
