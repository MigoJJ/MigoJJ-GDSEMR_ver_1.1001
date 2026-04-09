package com.emr.gds.soap;

import com.emr.gds.input.IAITextAreaManager;
import com.emr.gds.soap.config.PMHConfig;
import com.emr.gds.soap.presenter.PMHPresenter;
import com.emr.gds.soap.service.PMHService;
import com.emr.gds.soap.view.PMHView;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * JavaFX dialog for Past Medical History (PMH).
 * Left column: disease/category checkboxes in a grid
 * Right column: aligned editable fields (TextAreas) for details.
 *
 * --- Features from original version retained ---
 * - Abbreviation expansion from DB with CSV fallback.
 * - "Open EMRFMH" button functionality.
 * - Save inserts into provided external TextArea or shows in output area.
 * - Quit closes ONLY this window.
 * - Robust threading and error handling.
 *
 * --- UPGRADES Inspired by Swing EMRPMH ---
 * - UI Layout: Conditions are now in a dynamic multi-column grid for better space usage.
 * - Live Summary: The output area at the bottom updates in real-time as checkboxes are toggled.
 * - More Conditions: The list of conditions is more comprehensive.
 * - Copy to Clipboard: A new "Copy" button to easily export the summary.
 * - Specific Logic: Special handling for "All denied allergies" on save.
 */
public class EMRPMH extends Application {

    private final IAITextAreaManager textAreaManager;
    private final TextArea externalTarget;
    private final Map<String, String> abbrevMap;

    // -------- Constructors --------
    public EMRPMH() { this(null, null, Collections.emptyMap()); }
    public EMRPMH(IAITextAreaManager manager) { this(manager, null, Collections.emptyMap()); }
    public EMRPMH(IAITextAreaManager manager, TextArea externalTarget) { this(manager, externalTarget, Collections.emptyMap()); }
    public EMRPMH(IAITextAreaManager manager, TextArea externalTarget, Map<String, String> abbrevMap) {
        this.textAreaManager = manager;
        this.externalTarget = externalTarget;
        this.abbrevMap = (abbrevMap != null) ? abbrevMap : Collections.emptyMap();
    }

    // -------- JavaFX lifecycle --------
    @Override
    public void start(Stage primaryStage) {
        // Load configuration
        PMHConfig config = new PMHConfig();
        try {
            config = PMHConfig.load("/pmh-config.json");
        } catch (IOException e) {
            System.err.println("Failed to load PMH configuration in EMRPMH.start: " + e.getMessage());
            // Optionally show an alert to the user here
        }

        // Initialize service, view, and presenter
        PMHService pmhService = new PMHService(abbrevMap); // Pass abbrevMap to service
        PMHView pmhView = new PMHView();
        PMHPresenter pmhPresenter = new PMHPresenter(pmhService, pmhView, textAreaManager, externalTarget, abbrevMap);
        pmhPresenter.setStage(primaryStage); // Allow presenter to control the stage

        // Build UI from PMHView
        BorderPane root = pmhView.buildView(config.getNumColumns()); // Use numColumns from config

        Scene scene = new Scene(root, 1200, 1000); // Use original size
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) { pmhPresenter.onQuit(); e.consume(); }
        });
        // Add specific accelerators for Save (Ctrl+Enter is already handled by setOnKeyPressed) and Default
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
            pmhPresenter::onSave
        );
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN),
            pmhPresenter::applyDefaultDots
        );
        pmhView.applySceneStyling(scene); // Apply CSS
        primaryStage.setScene(scene);

        pmhPresenter.initializeUI(); // Initialize presenter, which populates the view and sets listeners
        primaryStage.show();
    }

    public void showDialog() {
        Platform.runLater(() -> {
            Stage s = new Stage();
            s.setTitle("EMR - Past Medical History (PMH) - Upgraded"); // Set title here
            
            // Load configuration
            PMHConfig config = new PMHConfig();
            try {
                config = PMHConfig.load("/pmh-config.json");
            } catch (IOException e) {
                System.err.println("Failed to load PMH configuration in EMRPMH.showDialog: " + e.getMessage());
                // Optionally show an alert to the user here
            }

            PMHService pmhService = new PMHService(abbrevMap);
            PMHView pmhView = new PMHView();
            PMHPresenter pmhPresenter = new PMHPresenter(pmhService, pmhView, textAreaManager, externalTarget, abbrevMap);
            pmhPresenter.setStage(s);

            BorderPane root = pmhView.buildView(config.getNumColumns());
            Scene scene = new Scene(root, 1200, 1000); // Use original size
            scene.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE) { pmhPresenter.onQuit(); e.consume(); }
            });
            // Add specific accelerators for Save (Ctrl+Enter is already handled by setOnKeyPressed) and Default
            scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
                pmhPresenter::onSave
            );
            scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN),
                pmhPresenter::applyDefaultDots
            );
            pmhView.applySceneStyling(scene);
            s.setScene(scene);
            
            pmhPresenter.initializeUI();
            s.initModality(Modality.NONE); // Keep original modality
            s.show();
        });
    }

    // CSS will now be handled by PMHView
    
    // -------- Actions --------
    // --- Actions and Helpers are now handled by PMHPresenter and PMHService ---

    public static void main(String[] args) {
        launch(args);
    }
}