package com.emr.gds.features.thyroid;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ThyroidRiskPane extends VBox {

    private final ThyroidEntry entry;
    
    // ATA Risk
    private final CheckBox chkGrossExt = new CheckBox("Gross Extrathyroidal Ext.");
    private final CheckBox chkIncomplete = new CheckBox("Incomplete Resection");
    private final CheckBox chkDistantMets = new CheckBox("Distant Mets");
    private final CheckBox chkAggressive = new CheckBox("Aggressive Histology");
    private final CheckBox chkVascularInv = new CheckBox("Vascular Invasion");
    private final TextField txtLymphCount = new TextField();
    private final TextField txtNodeSize = new TextField();
    private final Label lblAtaRisk = new Label("ATA Risk: Low");

    // TI-RADS
    private final ComboBox<ThyroidRiskCalculator.TiRadsFeature> cmbComp = new ComboBox<>();
    private final ComboBox<ThyroidRiskCalculator.TiRadsFeature> cmbEcho = new ComboBox<>();
    private final ComboBox<ThyroidRiskCalculator.TiRadsFeature> cmbShape = new ComboBox<>();
    private final ComboBox<ThyroidRiskCalculator.TiRadsFeature> cmbMargin = new ComboBox<>();
    private final ComboBox<ThyroidRiskCalculator.TiRadsFeature> cmbFoci = new ComboBox<>();
    private final Label lblTiRadsResult = new Label("TI-RADS: -");

    public ThyroidRiskPane(ThyroidEntry entry) {
        this.entry = entry;
        initControls();
        buildLayout();
        setSpacing(10);
        setPadding(new Insets(10));
    }

    private void initControls() {
        // Risk - ATA
        txtLymphCount.setPromptText("# Nodes");
        txtNodeSize.setPromptText("Max size (cm)");
        lblAtaRisk.setStyle("-fx-font-weight: bold; -fx-text-fill: #2980b9;");

        // Risk - TI-RADS
        cmbComp.getItems().setAll(ThyroidRiskCalculator.TiRadsFeature.COMP_CYSTIC_SPONGI, ThyroidRiskCalculator.TiRadsFeature.COMP_MIXED, ThyroidRiskCalculator.TiRadsFeature.COMP_SOLID);
        cmbEcho.getItems().setAll(ThyroidRiskCalculator.TiRadsFeature.ECHO_ANECHOIC, ThyroidRiskCalculator.TiRadsFeature.ECHO_HYPER_ISO, ThyroidRiskCalculator.TiRadsFeature.ECHO_HYPO, ThyroidRiskCalculator.TiRadsFeature.ECHO_VERY_HYPO);
        cmbShape.getItems().setAll(ThyroidRiskCalculator.TiRadsFeature.SHAPE_WIDER, ThyroidRiskCalculator.TiRadsFeature.SHAPE_TALLER);
        cmbMargin.getItems().setAll(ThyroidRiskCalculator.TiRadsFeature.MARGIN_SMOOTH, ThyroidRiskCalculator.TiRadsFeature.MARGIN_LOBULATED, ThyroidRiskCalculator.TiRadsFeature.MARGIN_EXTRA);
        cmbFoci.getItems().setAll(ThyroidRiskCalculator.TiRadsFeature.FOCI_NONE, ThyroidRiskCalculator.TiRadsFeature.FOCI_MACRO, ThyroidRiskCalculator.TiRadsFeature.FOCI_RIM, ThyroidRiskCalculator.TiRadsFeature.FOCI_PUNCTATE);
        
        lblTiRadsResult.setWrapText(true);
        lblTiRadsResult.setStyle("-fx-font-weight: bold; -fx-text-fill: #8e44ad;");

        // Listeners
        chkGrossExt.setOnAction(e -> updateAtaRisk());
        chkIncomplete.setOnAction(e -> updateAtaRisk());
        chkDistantMets.setOnAction(e -> updateAtaRisk());
        chkAggressive.setOnAction(e -> updateAtaRisk());
        chkVascularInv.setOnAction(e -> updateAtaRisk());
        txtLymphCount.textProperty().addListener((o, old, v) -> updateAtaRisk());
        txtNodeSize.textProperty().addListener((o, old, v) -> updateAtaRisk());

        cmbComp.setOnAction(e -> updateTiRads());
        cmbEcho.setOnAction(e -> updateTiRads());
        cmbShape.setOnAction(e -> updateTiRads());
        cmbMargin.setOnAction(e -> updateTiRads());
        cmbFoci.setOnAction(e -> updateTiRads());
    }

    private void buildLayout() {
        Label lblTirads = new Label("ACR TI-RADS Calculator");
        lblTirads.setStyle("-fx-font-weight: bold; -fx-underline: true;");
        
        GridPane tiradsGrid = new GridPane();
        tiradsGrid.setHgap(10); 
        tiradsGrid.setVgap(5);
        tiradsGrid.addRow(0, new Label("Composition:"), cmbComp);
        tiradsGrid.addRow(1, new Label("Echogenicity:"), cmbEcho);
        tiradsGrid.addRow(2, new Label("Shape:"), cmbShape);
        tiradsGrid.addRow(3, new Label("Margin:"), cmbMargin);
        tiradsGrid.addRow(4, new Label("Echogenic Foci:"), cmbFoci);
        
        HBox tiradsBox = new HBox(20, tiradsGrid, lblTiRadsResult);
        HBox.setHgrow(lblTiRadsResult, Priority.ALWAYS);
        lblTiRadsResult.setMaxWidth(300);

        Label lblAta = new Label("ATA Risk Stratification (DTC)");
        lblAta.setStyle("-fx-font-weight: bold; -fx-underline: true;");
        
        GridPane ataGrid = new GridPane();
        ataGrid.setHgap(15);
        ataGrid.setVgap(5);
        ataGrid.add(chkGrossExt, 0, 0);
        ataGrid.add(chkIncomplete, 1, 0);
        ataGrid.add(chkDistantMets, 2, 0);
        ataGrid.add(chkAggressive, 0, 1);
        ataGrid.add(chkVascularInv, 1, 1);
        
        HBox nodeBox = new HBox(5, new Label("Nodes #"), txtLymphCount, new Label("Max Size"), txtNodeSize);
        ataGrid.add(nodeBox, 0, 2, 3, 1);

        getChildren().addAll(lblTirads, tiradsBox, new Separator(), lblAta, ataGrid, lblAtaRisk);
    }

    private void updateAtaRisk() {
        try {
            int nodes = txtLymphCount.getText().isEmpty() ? 0 : Integer.parseInt(txtLymphCount.getText());
            double size = txtNodeSize.getText().isEmpty() ? 0 : Double.parseDouble(txtNodeSize.getText());
            String risk = ThyroidRiskCalculator.calculateAtaRisk(
                    chkGrossExt.isSelected(), chkIncomplete.isSelected(), chkDistantMets.isSelected(),
                    chkAggressive.isSelected(), chkVascularInv.isSelected(), nodes, size
            );
            lblAtaRisk.setText("ATA Risk: " + risk);
        } catch (NumberFormatException e) {
            lblAtaRisk.setText("ATA Risk: Error");
        }
    }

    private void updateTiRads() {
        if (cmbComp.getValue() != null && cmbEcho.getValue() != null && cmbShape.getValue() != null && cmbMargin.getValue() != null && cmbFoci.getValue() != null) {
            ThyroidRiskCalculator.TiRadsResult res = ThyroidRiskCalculator.calculateTiRads(cmbComp.getValue(), cmbEcho.getValue(), cmbShape.getValue(), cmbMargin.getValue(), cmbFoci.getValue());
            lblTiRadsResult.setText("TI-RADS: " + res.level + "\nScore: " + res.score + "\n" + res.recommendation);
        }
    }

    public void loadFromEntry() {
        if (entry.getGrossExtrathyroidalExtension() != null) chkGrossExt.setSelected(entry.getGrossExtrathyroidalExtension());
        if (entry.getIncompleteResection() != null) chkIncomplete.setSelected(entry.getIncompleteResection());
        if (entry.getDistantMetastases() != null) chkDistantMets.setSelected(entry.getDistantMetastases());
        if (entry.getAggressiveHistology() != null) chkAggressive.setSelected(entry.getAggressiveHistology());
        if (entry.getVascularInvasion() != null) chkVascularInv.setSelected(entry.getVascularInvasion());
        if (entry.getLymphNodeCount() != null) txtLymphCount.setText(String.valueOf(entry.getLymphNodeCount()));
        if (entry.getLargestNodeSizeCm() != null) txtNodeSize.setText(String.valueOf(entry.getLargestNodeSizeCm()));
        updateAtaRisk();
    }

    public void saveToEntry() {
        entry.setGrossExtrathyroidalExtension(chkGrossExt.isSelected());
        entry.setIncompleteResection(chkIncomplete.isSelected());
        entry.setDistantMetastases(chkDistantMets.isSelected());
        entry.setAggressiveHistology(chkAggressive.isSelected());
        entry.setVascularInvasion(chkVascularInv.isSelected());
        try { entry.setLymphNodeCount(Integer.parseInt(txtLymphCount.getText())); } catch (NumberFormatException ignored) {}
        try { entry.setLargestNodeSizeCm(Double.parseDouble(txtNodeSize.getText())); } catch (NumberFormatException ignored) {}
        
        if (cmbComp.getValue() != null && cmbEcho.getValue() != null && cmbShape.getValue() != null && cmbMargin.getValue() != null && cmbFoci.getValue() != null) {
            ThyroidRiskCalculator.TiRadsResult res = ThyroidRiskCalculator.calculateTiRads(cmbComp.getValue(), cmbEcho.getValue(), cmbShape.getValue(), cmbMargin.getValue(), cmbFoci.getValue());
            entry.setTiRadsScore(res.score);
            entry.setTiRadsLevel(res.level);
        }
        entry.setAtaRisk(lblAtaRisk.getText().replace("ATA Risk: ", ""));
    }
}
