package com.emr.gds.features.thyroid;

import com.emr.gds.context.AppContext;
import com.emr.gds.input.IAIMain;
import com.emr.gds.input.IAITextAreaManager;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThyroidController {
    private static final Logger logger = LoggerFactory.getLogger(ThyroidController.class);

    private final ThyroidEntry entry;
    private final ThyroidSummaryService summaryService = new ThyroidSummaryService();
    
    private ThyroidOverviewPane overviewPane;
    private ThyroidRiskPane riskPane;
    private ThyroidSymptomsPane symptomsPane;
    private ThyroidExamPane examPane;
    private ThyroidLabsPane labsPane;
    private ThyroidTreatmentPane treatmentPane;
    private ThyroidFollowUpPane followUpPane;

    public ThyroidController(ThyroidEntry entry) {
        this.entry = entry;
    }

    public void setPanes(ThyroidOverviewPane overviewPane, ThyroidRiskPane riskPane, ThyroidSymptomsPane symptomsPane, 
                         ThyroidExamPane examPane, ThyroidLabsPane labsPane, ThyroidTreatmentPane treatmentPane, 
                         ThyroidFollowUpPane followUpPane) {
        this.overviewPane = overviewPane;
        this.riskPane = riskPane;
        this.symptomsPane = symptomsPane;
        this.examPane = examPane;
        this.labsPane = labsPane;
        this.treatmentPane = treatmentPane;
        this.followUpPane = followUpPane;
    }

    public void generateSummary() {
        saveAllToEntry();
        String summary = summaryService.buildSpecialistSummary(entry, overviewPane.getSelectedConditions(), "");
        followUpPane.getTxtSummaryOutput().setText(summary);
        entry.setProblemListSummary(summary);
        logger.debug("Generated specialist summary.");
    }

    public void handleAiAssist() {
        saveAllToEntry();
        String findings = summaryService.buildSpecialistSummary(entry, overviewPane.getSelectedConditions(), "");
        String prompt = AppContext.getInstance().getAiAssistantService().buildThyroidPrompt(findings);
        
        followUpPane.getTxtSummaryOutput().setText("AI is thinking... please wait.");
        followUpPane.getBtnAiAssist().setDisable(true);

        AppContext.getInstance().getAiAssistantService().generateClinicalDraft(prompt)
            .thenAccept(draft -> {
                Platform.runLater(() -> {
                    followUpPane.getTxtSummaryOutput().setText(draft);
                    followUpPane.getBtnAiAssist().setDisable(false);
                    logger.info("AI draft successfully generated and displayed.");
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    followUpPane.getTxtSummaryOutput().setText("AI Error: " + ex.getMessage());
                    followUpPane.getBtnAiAssist().setDisable(false);
                });
                return null;
            });
    }

    public void saveAndQuit(Stage stage) {
        generateSummary();
        String summaryText = followUpPane.getTxtSummaryOutput().getText().trim();
        
        IAIMain.getManagerSafely().ifPresentOrElse(
            manager -> {
                String textToAppend = summaryText.endsWith("\n") ? summaryText : summaryText + "\n";
                manager.appendTextToSection(IAITextAreaManager.AREA_PI, textToAppend);
                logger.info("Saved thyroid summary to EMR.");
            },
            () -> {
                logger.error("EMR mainframe not connected.");
                new Alert(Alert.AlertType.ERROR, "EMR mainframe is not connected.").showAndWait();
            }
        );

        if (stage != null) {
            stage.close();
        }
    }

    public void saveAllToEntry() {
        overviewPane.saveToEntry();
        riskPane.saveToEntry();
        symptomsPane.saveToEntry();
        examPane.saveToEntry();
        labsPane.saveToEntry();
        treatmentPane.saveToEntry();
        followUpPane.saveToEntry();
    }

    public void loadAllFromEntry() {
        overviewPane.loadFromEntry();
        riskPane.loadFromEntry();
        symptomsPane.loadFromEntry();
        examPane.loadFromEntry();
        labsPane.loadFromEntry();
        treatmentPane.loadFromEntry();
        followUpPane.loadFromEntry();
    }
}
