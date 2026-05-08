package com.emr.gds.features.thyroid;

import com.emr.gds.util.StageSizing;
import com.emr.gds.features.medication.controller.MainController;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Orchestrator Pane for Thyroid EMR entry.
 * Refactored into a modular architecture with dedicated sub-panes and a controller.
 */
public class ThyroidPane extends VBox {

    private final ThyroidEntry entry;
    private final ThyroidController controller;

    private ThyroidOverviewPane overviewPane;
    private ThyroidRiskPane riskPane;
    private ThyroidSymptomsPane symptomsPane;
    private ThyroidExamPane examPane;
    private ThyroidLabsPane labsPane;
    private ThyroidTreatmentPane treatmentPane;
    private ThyroidFollowUpPane followUpPane;

    public ThyroidPane(ThyroidEntry entry) {
        this.entry = (entry != null) ? entry : new ThyroidEntry();
        this.controller = new ThyroidController(this.entry);
        
        initSubPanes();
        buildLayout();
        configureActions();
        
        controller.loadAllFromEntry();
    }

    private void initSubPanes() {
        overviewPane = new ThyroidOverviewPane(entry);
        riskPane = new ThyroidRiskPane(entry);
        symptomsPane = new ThyroidSymptomsPane(entry);
        examPane = new ThyroidExamPane(entry);
        labsPane = new ThyroidLabsPane(entry);
        treatmentPane = new ThyroidTreatmentPane(entry);
        followUpPane = new ThyroidFollowUpPane(entry);
        
        controller.setPanes(overviewPane, riskPane, symptomsPane, examPane, labsPane, treatmentPane, followUpPane);
    }

    private void buildLayout() {
        setSpacing(8);
        setPadding(new Insets(10));

        Accordion accordion = new Accordion(
                styledPane("1. Overview & Patient", overviewPane),
                styledPane("2. Risk Stratification & Tools", riskPane),
                styledPane("3. Symptoms", symptomsPane),
                styledPane("4. Physical Exam", examPane),
                styledPane("5. Labs", labsPane),
                styledPane("6. Treatment", treatmentPane),
                styledPane("7. Plan & Summary", followUpPane)
        );
        accordion.setExpandedPane(accordion.getPanes().get(0));

        getChildren().add(accordion);
        VBox.setVgrow(accordion, Priority.ALWAYS);
    }

    private void configureActions() {
        treatmentPane.getBtnOpenEmrHelper().setOnAction(e -> openEmrMedicationHelper());
        followUpPane.getBtnGenerateSummary().setOnAction(e -> controller.generateSummary());
        followUpPane.getBtnAiAssist().setOnAction(e -> controller.handleAiAssist());
        followUpPane.getBtnSaveQuit().setOnAction(e -> controller.saveAndQuit((Stage) getScene().getWindow()));
    }

    private void openEmrMedicationHelper() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/emr/gds/features/medication/main.fxml"));
            Parent root = loader.load();
            MainController medicationController = loader.getController();
            medicationController.setSelectedCategory("Thyroid");

            Stage stage = new Stage();
            stage.setTitle("EMR Helper – Thyroid");
            stage.setScene(new Scene(root));
            StageSizing.fitToScreen(stage, 0.8, 0.9, 1100, 700);
            stage.show();
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "Unable to open EMR Helper: " + ex.getMessage()).showAndWait();
        }
    }

    private TitledPane styledPane(String title, Node content) {
        TitledPane pane = new TitledPane();
        pane.setContent(content);
        Label header = new Label(title);
        header.setStyle("-fx-font-weight: bold; -fx-font-style: italic; -fx-font-size: 110%; -fx-text-fill: #0d3d8f;");
        pane.setGraphic(header);
        pane.setText(null);
        return pane;
    }
}
