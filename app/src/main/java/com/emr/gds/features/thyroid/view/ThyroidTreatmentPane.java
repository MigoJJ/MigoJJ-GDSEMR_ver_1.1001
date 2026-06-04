package com.emr.gds.features.thyroid.view;

import com.emr.gds.features.thyroid.model.ThyroidEntry;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ThyroidTreatmentPane extends VBox {

    private final ThyroidEntry entry;
    
    private final TextField txtLt4Dose = new TextField();
    private final TextField txtAtdName = new TextField();
    private final TextField txtAtdDose = new TextField();
    private final TextField txtBetaBlockerName = new TextField();
    private final TextField txtBetaBlockerDose = new TextField();
    private final Button btnOpenEmrHelper = new Button("Open EMR Helper");

    public ThyroidTreatmentPane(ThyroidEntry entry) {
        this.entry = entry;
        initControls();
        buildLayout();
        setSpacing(10);
        setPadding(new Insets(10));
    }

    private void initControls() {
        txtLt4Dose.setPromptText("LT4 (mcg)");
        txtAtdName.setPromptText("ATD Name");
        txtAtdDose.setPromptText("Dose (mg)");
        txtBetaBlockerName.setPromptText("BB Name");
        txtBetaBlockerDose.setPromptText("BB Dose");
    }

    private void buildLayout() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        grid.addRow(0, new Label("Levothyroxine (mcg)"), txtLt4Dose);
        grid.addRow(1, new Label("Antithyroid Drug"), txtAtdName, new Label("Dose (mg)"), txtAtdDose);
        grid.addRow(2, new Label("Beta Blocker"), txtBetaBlockerName, new Label("Dose"), txtBetaBlockerDose);
        HBox helperBox = new HBox(btnOpenEmrHelper);
        helperBox.setAlignment(Pos.CENTER_LEFT);
        grid.add(helperBox, 0, 3, 4, 1);

        getChildren().add(grid);
    }

    public void loadFromEntry() {
        if (entry.getLt4DoseMcgPerDay() != null) txtLt4Dose.setText(String.valueOf(entry.getLt4DoseMcgPerDay()));
        txtAtdName.setText(entry.getAtdName());
        if (entry.getAtdDoseMgPerDay() != null) txtAtdDose.setText(String.valueOf(entry.getAtdDoseMgPerDay()));
        txtBetaBlockerName.setText(entry.getBetaBlockerName());
        txtBetaBlockerDose.setText(entry.getBetaBlockerDose());
    }

    public void saveToEntry() {
        entry.setLt4DoseMcgPerDay(parseNullableDouble(txtLt4Dose));
        entry.setAtdName(txtAtdName.getText());
        entry.setAtdDoseMgPerDay(parseNullableDouble(txtAtdDose));
        entry.setBetaBlockerName(txtBetaBlockerName.getText());
        entry.setBetaBlockerDose(txtBetaBlockerDose.getText());
    }

    public Button getBtnOpenEmrHelper() {
        return btnOpenEmrHelper;
    }

    private Double parseNullableDouble(TextField field) {
        String value = field.getText();
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
