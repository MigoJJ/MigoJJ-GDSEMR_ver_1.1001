package com.emr.gds.features.thyroid;

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

        VBox noteBox = new VBox(5, new Label("Physical Exam Notes:"), txtPhysicalExamNote);
        
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
        entry.setPhysicalExamNote(txtPhysicalExamNote.getText());
    }

    public Map<String, List<CheckBox>> getExamSectionMap() {
        return examSectionMap;
    }
}
