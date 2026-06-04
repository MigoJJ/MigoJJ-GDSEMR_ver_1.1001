package com.emr.gds.features.thyroid.view;

import com.emr.gds.features.thyroid.model.ThyroidEntry;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ThyroidSymptomsPane extends VBox {

    private final ThyroidEntry entry;
    private final Label symptomSummary = new Label("No symptoms selected");
    private final TextField txtSymptomNegatives = new TextField();
    private final Map<ThyroidEntry.Symptom, CheckBox> symptomCheckboxes = new LinkedHashMap<>();
    private static final Map<String, List<ThyroidEntry.Symptom>> SYMPTOM_GROUPS = buildSymptomGroups();

    public ThyroidSymptomsPane(ThyroidEntry entry) {
        this.entry = entry;
        initControls();
        buildLayout();
        setSpacing(10);
        setPadding(new Insets(10));
    }

    private void initControls() {
        symptomSummary.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        symptomSummary.setWrapText(true);
        symptomSummary.setMaxWidth(500);
        txtSymptomNegatives.setPromptText("Recent negatives (e.g., denies tremor, weight loss)");
    }

    private void buildLayout() {
        Label intro = new Label("Select common thyroid-related symptoms");
        intro.setStyle("-fx-font-weight: bold;");

        VBox symptomBox = new VBox(12);
        symptomBox.setFillWidth(true);

        SYMPTOM_GROUPS.forEach((group, symptoms) -> {
            Label groupLabel = new Label(group);
            groupLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0d3d8f;");
            GridPane grid = new GridPane();
            grid.setHgap(12);
            grid.setVgap(6);

            for (int i = 0; i < symptoms.size(); i++) {
                ThyroidEntry.Symptom symptom = symptoms.get(i);
                CheckBox cb = new CheckBox(symptom.getLabel());
                cb.setOnAction(e -> updateSymptomSummary());
                symptomCheckboxes.put(symptom, cb);

                int col = i % 2;
                int row = i / 2;
                grid.add(cb, col, row);
            }

            symptomBox.getChildren().addAll(groupLabel, grid, new Separator());
        });

        VBox negativesBox = new VBox(4, new Label("Recent negatives / denials:"), txtSymptomNegatives);
        getChildren().addAll(intro, symptomBox, symptomSummary, negativesBox);
    }

    private void updateSymptomSummary() {
        StringBuilder sb = new StringBuilder("Selected Symptoms: ");
        boolean first = true;
        for (var entry : symptomCheckboxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                if (!first) sb.append(", ");
                sb.append(entry.getKey().name().toLowerCase().replace("hyper_", "").replace("hypo_", "").replace("_", " "));
                first = false;
            }
        }
        if (first) sb.append("None");
        symptomSummary.setText(sb.toString());
    }

    public void loadFromEntry() {
        List<ThyroidEntry.Symptom> selected = entry.getSymptoms();
        symptomCheckboxes.forEach((s, cb) -> cb.setSelected(selected.contains(s)));
        txtSymptomNegatives.setText(entry.getSymptomNegatives());
        updateSymptomSummary();
    }

    public void saveToEntry() {
        List<ThyroidEntry.Symptom> selected = new ArrayList<>();
        symptomCheckboxes.forEach((s, cb) -> {
            if (cb.isSelected()) selected.add(s);
        });
        entry.setSymptoms(selected);
        entry.setSymptomNegatives(txtSymptomNegatives.getText());
    }

    private static Map<String, List<ThyroidEntry.Symptom>> buildSymptomGroups() {
        Map<String, List<ThyroidEntry.Symptom>> groups = new LinkedHashMap<>();
        List<ThyroidEntry.Symptom> hyper = new ArrayList<>();
        List<ThyroidEntry.Symptom> hypo = new ArrayList<>();
        List<ThyroidEntry.Symptom> general = new ArrayList<>();

        for (ThyroidEntry.Symptom symptom : ThyroidEntry.Symptom.values()) {
            String name = symptom.name();
            if (name.startsWith("HYPER_")) {
                hyper.add(symptom);
            } else if (name.startsWith("HYPO_")) {
                hypo.add(symptom);
            } else {
                general.add(symptom);
            }
        }

        hyper.sort((a, b) -> a.getLabel().compareToIgnoreCase(b.getLabel()));
        hypo.sort((a, b) -> a.getLabel().compareToIgnoreCase(b.getLabel()));
        general.sort((a, b) -> a.getLabel().compareToIgnoreCase(b.getLabel()));

        groups.put("Hyperthyroidism", hyper);
        groups.put("Hypothyroidism", hypo);
        groups.put("General / Other", general);
        return groups;
    }
}
