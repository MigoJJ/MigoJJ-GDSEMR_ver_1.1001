package com.emr.gds.features.thyroid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ThyroidSummaryService {

    private static final String TSH_REF = "0.4-4.0 uIU/mL";
    private static final String FT4_REF = "0.8-1.8 ng/dL";
    private static final String FT3_REF = "2.3-4.2 pg/mL";
    private static final String T3_REF = "80-200 ng/dL";
    private static final String TPOAB_REF = "≤34.0 IU/mL";
    private static final String TG_REF = "3.50-77.00 ng/mL";
    private static final String TGAB_REF = "≤115.0 IU/mL";
    private static final String TRAB_REF = "<1.75 IU/L";
    private static final String CALCITONIN_REF = "M:≤18.2, F:≤11.5 pg/mL";
    private static final String REVT3_REF = "8-25 ng/dL";

    public String buildSpecialistSummary(ThyroidEntry e, Map<String, List<String>> selectedConditions, String tiRadsResult) {
        List<String> lines = new ArrayList<>();
        String visit = (e.getVisitType() != null) ? e.getVisitType() + " visit" : "Thyroid specialist evaluation";
        lines.add("* Visit: " + visit);

        if (e.getCategories().isEmpty()) {
            lines.add("     | Dx: Thyroid screening/evaluation");
        } else {
            List<String> dx = e.getCategories().stream().map(Object::toString).toList();
            lines.add("     | Dx: " + String.join(", ", dx));
        }

        if (!selectedConditions.isEmpty()) {
            lines.add("     | Conditions checklist:");
            for (var entryGroup : selectedConditions.entrySet()) {
                lines.add("     |   " + entryGroup.getKey() + ": " + String.join("; ", entryGroup.getValue()));
            }
        }

        boolean hasPhysicalExamNote = e.getPhysicalExamNote() != null && !e.getPhysicalExamNote().isBlank();
        if (!hasPhysicalExamNote && e.getGoiterSize() != null && !e.getGoiterSize().isBlank()) {
            String goiterSize = e.getGoiterSize();
            if (!goiterSize.toLowerCase().contains("cc") && !goiterSize.toLowerCase().contains("cm")) {
                try {
                    Double.parseDouble(goiterSize);
                    goiterSize += " cc";
                } catch (NumberFormatException ignored) {}
            }
            lines.add("     | Physical Exam: Goiter size " + goiterSize);
        }
        addPhysicalExamBlock(lines, e.getPhysicalExamNote());
        
        String negatives = (e.getSymptomNegatives() != null) ? e.getSymptomNegatives().trim() : "";
        if (!e.getSymptoms().isEmpty() || !negatives.isBlank()) {
            List<String> syms = e.getSymptoms().stream().map(ThyroidEntry.Symptom::getLabel).toList();
            StringBuilder symptomLine = new StringBuilder("     | Symptoms: ");
            if (!syms.isEmpty()) {
                symptomLine.append(String.join("; ", syms));
            } else {
                symptomLine.append("None reported");
            }
            if (!negatives.isBlank()) {
                symptomLine.append("; Recent negatives: ").append(negatives);
            }
            lines.add(symptomLine.toString());
        }

        List<String> statusParts = new ArrayList<>();
        if (e.getCategories().contains(ThyroidEntry.MainCategory.HYPOTHYROIDISM)) {
            StringBuilder hypoLine = new StringBuilder("Hypothyroidism ");
            if (e.getHypoEtiology() != null) hypoLine.append(e.getHypoEtiology()).append(". ");
            hypoLine.append(Boolean.TRUE.equals(e.isHypoOvert()) ? "Overt." : "Subclinical.");
            if (e.getLt4DoseMcgPerDay() != null) {
                hypoLine.append(" LT4 ").append(e.getLt4DoseMcgPerDay()).append(" mcg.");
                if (e.getPatientWeightKg() != null) {
                    double est = ThyroidRiskCalculator.calculateFullReplacementDose(e.getPatientWeightKg());
                    hypoLine.append(" Est ").append((int) est).append(" mcg.");
                }
            }
            statusParts.add(hypoLine.toString());
        }

        if (e.getCategories().contains(ThyroidEntry.MainCategory.HYPERTHYROIDISM)) {
            StringBuilder hyperLine = new StringBuilder("Hyperthyroidism ");
            if (e.getHyperEtiology() != null) hyperLine.append(e.getHyperEtiology()).append(". ");
            hyperLine.append(Boolean.TRUE.equals(e.isHyperActive()) ? "Uncontrolled/Active." : "Controlled/Remission.");
            if (e.getAtdName() != null) {
                hyperLine.append(" On ").append(e.getAtdName()).append(" ").append(e.getAtdDoseMgPerDay()).append(" mg.");
            }
            statusParts.add(hyperLine.toString());
        }

        if (e.getCategories().contains(ThyroidEntry.MainCategory.CANCER)) {
            StringBuilder caLine = new StringBuilder("Thyroid Cancer ");
            if (e.getCancerHistology() != null) caLine.append(e.getCancerHistology()).append(". ");
            if (e.getTnmStage() != null && !e.getTnmStage().isBlank()) caLine.append("TNM: ").append(e.getTnmStage()).append(". ");
            if (e.getAtaRisk() != null && !e.getAtaRisk().equals("Low Risk")) {
                caLine.append(e.getAtaRisk()).append(" (path features). ");
            } else {
                caLine.append("Low Risk Stratification. ");
            }
            if (e.getTg() != null) caLine.append("Tg: ").append(e.getTg()).append(" ng/mL. ");
            if (e.getCancerStatus() != null && !e.getCancerStatus().isBlank()) caLine.append(e.getCancerStatus()).append(". ");
            statusParts.add(caLine.toString().trim());
        }
        if (!statusParts.isEmpty()) {
            lines.add("     | Status: " + statusParts.getFirst());
            for (int i = 1; i < statusParts.size(); i++) {
                lines.add("     | " + statusParts.get(i));
            }
        }

        List<String> labLines = new ArrayList<>();
        addLabLine(labLines, "TSH", e.getTsh(), TSH_REF);
        addLabLine(labLines, "fT4", e.getFreeT4(), FT4_REF);
        addLabLine(labLines, "fT3", e.getFreeT3(), FT3_REF);
        addLabLine(labLines, "T3", e.getTotalT3(), T3_REF);
        addLabLine(labLines, "TPOAb", e.getTpoAb(), TPOAB_REF);
        addLabLine(labLines, "Tg", e.getTg(), TG_REF);
        addLabLine(labLines, "TgAb", e.getTgAb(), TGAB_REF);
        addLabLine(labLines, "TRAb", e.getTrab(), TRAB_REF);
        addLabLine(labLines, "Calcitonin", e.getCalcitonin(), CALCITONIN_REF);
        addLabLine(labLines, "revT3", e.getReverseT3(), REVT3_REF);

        if (!labLines.isEmpty()) {
            String datePart = (e.getLastLabDate() != null) ? " (" + e.getLastLabDate() + ")" : "";
            lines.add("     | Labs" + datePart + ":");
            lines.addAll(labLines);
        }

        if (tiRadsResult != null && tiRadsResult.contains("Score")) {
            lines.add("     | Nodule/TI-RADS: " + tiRadsResult.replace("\n", ", "));
        } else if (e.getTiRadsLevel() != null && e.getTiRadsScore() != null) {
            lines.add("     | Nodule/TI-RADS: " + e.getTiRadsLevel() + ", Score: " + e.getTiRadsScore());
        }

        addTextLine(lines, "Ultrasound" + formatDate(e.getUsDate()), e.getUsSummary());
        addTextLine(lines, "Scan" + formatDate(e.getScanDate()), e.getScanSummary());

        String treatment = buildTreatmentLine(e);
        if (!treatment.isBlank()) {
            lines.add("     | Treatment: " + treatment);
        }

        String rai = buildRaiLine(e);
        if (!rai.isBlank()) {
            lines.add("     | RAI: " + rai);
        }

        addTextLine(lines, "Clinician Note", e.getClinicianNote());

        StringBuilder plan = new StringBuilder("Plan: ");
        if (e.getFollowUpInterval() != null) {
            plan.append("Follow up in ").append(e.getFollowUpInterval()).append(". ");
        }
        if (e.getFollowUpPlanText() != null && !e.getFollowUpPlanText().isBlank()) {
            plan.append(e.getFollowUpPlanText().replaceAll("\\R+", "; ").trim());
        }
        lines.add("     | " + plan.toString().trim());

        return String.join("\n", lines);
    }

    private void addLabLine(List<String> lines, String name, Double value, String ref) {
        if (value == null) return;
        String indicator = getLabIndicator(value, ref);
        lines.add(String.format("          %-15s\t%-10.2f\t%-2s\t(%s)", name, value, indicator, ref));
    }

    private void addTextLine(List<String> lines, String label, String value) {
        if (value == null || value.isBlank()) return;
        lines.add("     | " + label + ": " + value.replaceAll("\\R+", "; ").trim());
    }

    private void addMultilineTextBlock(List<String> lines, String label, String value) {
        if (value == null || value.isBlank()) return;
        lines.add("     | " + label + ":");
        for (String rawLine : value.strip().split("\\R+")) {
            String line = rawLine.strip();
            if (line.isEmpty()) continue;
            if (line.endsWith(":")) {
                lines.add("     |\t" + line);
            } else if (line.startsWith("-")) {
                lines.add("     |\t\t" + line);
            } else {
                lines.add("     |\t" + line);
            }
        }
    }

    private void addPhysicalExamBlock(List<String> lines, String value) {
        if (value == null || value.isBlank()) return;
        for (String rawLine : value.strip().split("\\R+")) {
            String line = rawLine.strip();
            if (line.isEmpty()) continue;
            if (line.endsWith(":")) {
                lines.add("     |\t" + line);
            } else if (line.startsWith("-")) {
                lines.add("     |\t\t" + line);
            } else {
                lines.add("     |\t" + line);
            }
        }
    }

    private String buildTreatmentLine(ThyroidEntry e) {
        List<String> parts = new ArrayList<>();
        if (e.getLt4DoseMcgPerDay() != null) {
            parts.add("LT4 " + formatNumber(e.getLt4DoseMcgPerDay()) + " mcg/day");
        }
        if ((e.getAtdName() != null && !e.getAtdName().isBlank()) || e.getAtdDoseMgPerDay() != null) {
            String atd = (e.getAtdName() != null && !e.getAtdName().isBlank()) ? e.getAtdName().trim() : "ATD";
            if (e.getAtdDoseMgPerDay() != null) {
                atd += " " + formatNumber(e.getAtdDoseMgPerDay()) + " mg/day";
            }
            parts.add(atd);
        }
        if ((e.getBetaBlockerName() != null && !e.getBetaBlockerName().isBlank())
                || (e.getBetaBlockerDose() != null && !e.getBetaBlockerDose().isBlank())) {
            String betaBlocker = (e.getBetaBlockerName() != null && !e.getBetaBlockerName().isBlank())
                    ? e.getBetaBlockerName().trim()
                    : "Beta blocker";
            if (e.getBetaBlockerDose() != null && !e.getBetaBlockerDose().isBlank()) {
                betaBlocker += " " + e.getBetaBlockerDose().trim();
            }
            parts.add(betaBlocker);
        }
        if (e.getOtherMeds() != null && !e.getOtherMeds().isBlank()) {
            parts.add(e.getOtherMeds().replaceAll("\\R+", "; ").trim());
        }
        return String.join("; ", parts);
    }

    private String buildRaiLine(ThyroidEntry e) {
        List<String> parts = new ArrayList<>();
        if (e.getRaiDone() != null) {
            parts.add(Boolean.TRUE.equals(e.getRaiDone()) ? "done" : "not done");
        }
        if (e.getRaiDoseMci() != null) {
            parts.add(formatNumber(e.getRaiDoseMci()) + " mCi");
        }
        if (e.getRaiDate() != null) {
            parts.add(e.getRaiDate().toString());
        }
        return String.join("; ", parts);
    }

    private String formatDate(java.time.LocalDate date) {
        return date == null ? "" : " (" + date + ")";
    }

    private String formatNumber(Double value) {
        if (value == null) return "";
        if (value % 1 == 0) {
            return String.valueOf(value.intValue());
        }
        return String.valueOf(value);
    }

    private String getLabIndicator(Double value, String ref) {
        if (value == null || ref == null || ref.contains("M:") || ref.contains("F:")) return "";
        String[] parts = ref.split(" ");
        if (parts.length == 0) return "";
        String numericRef = parts[0];

        try {
            if (numericRef.contains("-")) {
                String[] range = numericRef.split("-");
                double low = Double.parseDouble(range[0]);
                double high = Double.parseDouble(range[1]);
                if (value < low) return "▽";
                if (value > high) return "▲";
            } else if (numericRef.startsWith("≤")) {
                if (value > Double.parseDouble(numericRef.substring(1))) return "▲";
            } else if (numericRef.startsWith("<")) {
                if (value >= Double.parseDouble(numericRef.substring(1))) return "▲";
            }
        } catch (Exception ignored) {}
        return "";
    }
}
