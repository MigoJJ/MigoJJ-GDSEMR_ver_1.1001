package com.emr.gds.features.thyroid.view;

import com.emr.gds.features.thyroid.model.ThyroidEntry;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ThyroidFollowUpPane extends VBox {

    private final ThyroidEntry entry;
    
    private final ComboBox<String> cmbFollowUpInterval = new ComboBox<>();
    private final TextArea txtFollowUpPlan = new TextArea();
    private final TextArea txtSummaryOutput = new TextArea();
    private final Button btnGenerateSummary = new Button("Generate Specialist Summary");
    private final Button btnAiAssist = new Button("AI Documentation Assist");
    private final Button btnSaveQuit = new Button("Save and Quit");

    public ThyroidFollowUpPane(ThyroidEntry entry) {
        this.entry = entry;
        initControls();
        buildLayout();
        setSpacing(10);
        setPadding(new Insets(10));
    }

    private void initControls() {
        cmbFollowUpInterval.getItems().addAll("3 months", "6 months", "12 months", "Custom");
        cmbFollowUpInterval.setPromptText("Interval");
        txtFollowUpPlan.setPromptText("Tests, Imaging, etc.");
        txtFollowUpPlan.setPrefRowCount(3);
        txtSummaryOutput.setPromptText("Specialist summary...");
        txtSummaryOutput.setWrapText(true);
        txtSummaryOutput.setPrefRowCount(10);
        txtSummaryOutput.setMinHeight(200);
        txtSummaryOutput.setStyle("-fx-control-inner-background: #fff5cc; -fx-border-color: #d35400; -fx-border-width: 2; -fx-font-family: 'Consolas'; -fx-font-size: 12px;");
        
        btnAiAssist.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-font-weight: bold;");
    }

    private void buildLayout() {
        VBox fupBox = new VBox(5, new Label("Next follow-up interval:"), cmbFollowUpInterval, new Label("Plan details:"), txtFollowUpPlan);
        HBox actionButtons = new HBox(10, btnGenerateSummary, btnAiAssist);
        getChildren().addAll(fupBox, new Separator(), actionButtons, txtSummaryOutput, btnSaveQuit);
    }

    public void loadFromEntry() {
        cmbFollowUpInterval.setValue(entry.getFollowUpInterval());
        txtFollowUpPlan.setText(entry.getFollowUpPlanText());
    }

    public void saveToEntry() {
        entry.setFollowUpInterval(cmbFollowUpInterval.getValue());
        entry.setFollowUpPlanText(txtFollowUpPlan.getText());
    }

    public Button getBtnGenerateSummary() { return btnGenerateSummary; }
    public Button getBtnAiAssist() { return btnAiAssist; }
    public Button getBtnSaveQuit() { return btnSaveQuit; }
    public TextArea getTxtSummaryOutput() { return txtSummaryOutput; }
}
