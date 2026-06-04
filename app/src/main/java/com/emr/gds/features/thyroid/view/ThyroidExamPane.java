package com.emr.gds.features.thyroid.view;

import com.emr.gds.features.thyroid.model.ThyroidEntry;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ThyroidExamPane extends VBox {

    private final ThyroidEntry entry;
    private final TextField txtGoiterSize = new TextField();
    private final TextArea txtPhysicalExamNote = new TextArea();
    private final Button btnGenerateNote = new Button("Generate Note");
    private final LinkedHashMap<String, List<CheckBox>> examSectionMap = new LinkedHashMap<>();

    private static final String[][] EXAM_SECTIONS = {
            {"Goiter Ruled", "Goiter ruled out", "Goiter ruled in Diffuse Enlargement",
                    "Goiter ruled in Nodular Enlargement", "Single Nodular Goiter", "Multiple Nodular Goiter"},
            {"Detect any nodules", "None", "Single nodule", "Multinodular Goiter"},
            {"Thyroid gland consistency", "Soft", "Soft to Firm", "Firm", "Cobble-stone", "Firm to Hard", "Hard"},
            {"Evaluate the thyroid gland for tenderness", "Tender", "Non-tender"},
            {"Systolic or continuous Bruit (y/n)", "Yes", "No"},
            {"DTR deep tendon reflex", "1+ = present but depressed", "2+ = normal / average",
                    "3+ = increased", "4+ = clonus", "Doctor has not performed DTR test"},
            {"TED: Thyroid Eye Disease", "Class 0: No signs or symptoms",
                    "Class 1: Only signs", "Class 2: Soft tissue involvement",
                    "Class 3: Proptosis", "Class 4: Extraocular muscle involvement",
                    "Class 5: Corneal involvement", "Class 6: Sight loss"}
    };

    private static final Map<String, String> EXAM_OUTPUT_LABELS = Map.of(
            "Thyroid gland consistency", "Consistency",
            "Evaluate the thyroid gland for tenderness", "Evaluate the tenderness",
            "DTR deep tendon reflex", "DTR ",
            "TED: Thyroid Eye Disease", "TED"
    );

    public ThyroidExamPane(ThyroidEntry entry) {
        this.entry = entry;
        initControls();
        buildLayout();
        setSpacing(10);
        setPadding(new Insets(10));
    }

    private void initControls() {
        txtGoiterSize.setPromptText("Goiter size (cm)");
        txtPhysicalExamNote.setPromptText("Physical Exam Notes");
        txtPhysicalExamNote.setPrefRowCount(3);
        txtPhysicalExamNote.setWrapText(true);
        btnGenerateNote.setOnAction(e -> txtPhysicalExamNote.setText(buildGeneratedNote()));
    }

    private void buildLayout() {
        VBox left = new VBox(10);
        VBox right = new VBox(10);
        left.setFillWidth(true);
        right.setFillWidth(true);

        int midpoint = (int) Math.ceil(EXAM_SECTIONS.length / 2.0);
        for (int idx = 0; idx < EXAM_SECTIONS.length; idx++) {
            String[] section = EXAM_SECTIONS[idx];
            if (section.length < 2) continue;
            Label label = new Label(section[0] + ":");
            label.setStyle("-fx-font-weight: bold;");
            VBox sectionBox = new VBox(4);
            sectionBox.getChildren().add(label);
            List<CheckBox> sectionChecks = new ArrayList<>();

            for (int i = 1; i < section.length; i++) {
                CheckBox cb = new CheckBox(section[i]);
                sectionChecks.add(cb);
                sectionBox.getChildren().add(cb);
            }
            examSectionMap.put(section[0], sectionChecks);

            sectionBox.setFillWidth(true);
            if (idx < midpoint) {
                left.getChildren().add(sectionBox);
            } else {
                right.getChildren().add(sectionBox);
            }
        }

        HBox split = new HBox(20, left, right);
        split.setFillHeight(true);

        HBox goiterBox = new HBox(10, new Label("Goiter size:"), txtGoiterSize);
        goiterBox.setAlignment(Pos.CENTER_LEFT);

        HBox noteHeader = new HBox(10, new Label("Physical Exam Notes:"), btnGenerateNote);
        noteHeader.setAlignment(Pos.CENTER_LEFT);
        VBox noteBox = new VBox(5, noteHeader, txtPhysicalExamNote);
        
        getChildren().addAll(split, new Separator(), goiterBox, noteBox);
    }

    public void loadFromEntry() {
        txtGoiterSize.setText(entry.getGoiterSize());
        txtPhysicalExamNote.setText(entry.getPhysicalExamNote());
        // For physical exam checkboxes, we need a way to map them back.
        // The original code uses updatePhysicalExamNotes which generates text.
        // For now, we'll focus on the structural split.
    }

    public void saveToEntry() {
        entry.setGoiterSize(txtGoiterSize.getText());
        entry.setPhysicalExamNote(buildCombinedPhysicalExamNote());
    }

    public Map<String, List<CheckBox>> getExamSectionMap() {
        return examSectionMap;
    }

    String buildGeneratedNote() {
        List<String> lines = new ArrayList<>();
        String goiterSize = txtGoiterSize.getText();
        if (goiterSize != null && !goiterSize.isBlank()) {
            lines.add("Goiter size: " + formatGoiterSize(goiterSize));
        }

        for (var section : examSectionMap.entrySet()) {
            List<String> selected = section.getValue().stream()
                    .filter(CheckBox::isSelected)
                    .map(CheckBox::getText)
                    .toList();
            if (!selected.isEmpty()) {
                String label = EXAM_OUTPUT_LABELS.getOrDefault(section.getKey(), section.getKey());
                lines.add(formatExamLine(label, selected));
            }
        }

        if (lines.isEmpty()) {
            return "";
        }
        return "Physical Exam:\n- " + String.join("\n- ", lines);
    }

    private String formatExamLine(String label, List<String> selected) {
        if ("TED".equals(label) && selected.size() == 1 && selected.get(0).contains(":")) {
            String[] parts = selected.get(0).split(":", 2);
            return label + ":  " + parts[0].trim() + ":  [ " + parts[1].trim() + " ]";
        }
        return label + ": " + selected.stream()
                .map(value -> "[ " + value + " ]")
                .collect(java.util.stream.Collectors.joining("; "));
    }

    private String formatGoiterSize(String goiterSize) {
        String value = goiterSize.trim();
        if (value.toLowerCase().contains("cc") || value.toLowerCase().contains("cm")) {
            return value;
        }
        try {
            Double.parseDouble(value);
            return "[ " + value + " ] cc";
        } catch (NumberFormatException ignored) {
            return value;
        }
    }

    private String buildCombinedPhysicalExamNote() {
        String generated = buildGeneratedNote();
        String existing = txtPhysicalExamNote.getText();
        existing = existing == null ? "" : existing.trim();

        if (generated.isBlank()) {
            return existing;
        }
        if (existing.isBlank() || existing.equals(generated) || existing.contains(generated)) {
            return generated;
        }
        return generated + "\nAdditional note: " + existing;
    }
}
