package com.emr.gds;

import com.emr.gds.core.db.AppDatabaseManager;
import com.emr.gds.repository.sqlite.SqliteAbbreviationRepository;
import com.emr.gds.repository.sqlite.SqlitePlanHistoryRepository;
import com.emr.gds.repository.sqlite.SqliteProblemRepository;
import com.emr.gds.repository.sqlite.SqliteReferenceRepository; // New import
import com.emr.gds.service.AbbreviationService;
import com.emr.gds.service.PlanHistoryService;
import com.emr.gds.service.ProblemListService;
import com.emr.gds.service.ReferenceService; // New import
import com.emr.gds.features.imaging.ChestXrayReviewStage;
import com.emr.gds.features.ekg.EkgReportStage;
import com.emr.gds.features.ekg.EkgSimpleReportApp;
import com.emr.gds.features.ekg.EkgQuickInterpreter;
import com.emr.gds.features.allergy.AllergyApp;
import com.emr.gds.features.bone.DexaRiskAssessmentApp;
import com.emr.gds.input.IAIFreqFrame;
import com.emr.gds.input.IAIFxTextAreaManager;
import com.emr.gds.input.IAIMain;
import com.emr.gds.input.IAITextAreaManager;
import com.emr.gds.shared.ui.IAMButtonAction;
import com.emr.gds.shared.ui.IAMFunctionkey;
import com.emr.gds.shared.ui.IAMProblemAction;
import com.emr.gds.shared.ui.IAMTextArea;
import com.emr.gds.shared.ui.IAMTextFormatUtil;
import com.emr.gds.shared.ui.TextAreaControlProcessor;
import com.emr.gds.context.AppContext;
import com.emr.gds.core.config.RuntimeEnvironment;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import com.emr.gds.features.medication.MedicationCategory;
import com.emr.gds.features.thyroid.ThyroidLauncher;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main JavaFX Application for GDSEMR ITTIA - EMR Prototype.
 */
public class IttiaApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(IttiaApp.class);

    // ================================
    // Constants
    // ================================
    private static final String APP_TITLE = " GDSEMRITTIA  EMR prototype(JAVAFX)";
    private static final int SCENE_WIDTH = 1350;
    private static final int SCENE_HEIGHT = 1000;
    private static final int INITIAL_FOCUS_AREA = 0; // Corresponds to the first text area

    // ================================
    // UI and Core Logic Components
    // ================================
    private IAMProblemAction problemAction;
    private IAMButtonAction buttonAction;
    private IAMTextArea textAreaManager;
    private IAIFreqFrame freqStage; // Manages the vital signs window
    private IAMFunctionkey functionKeyHandler;
    private Stage mainStage;
    
    // Profiling
    private long startTime;

    // ================================
    // Application Lifecycle
    // ================================

    /**
     * Main entry point for the JavaFX application.
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Initializes and starts the primary stage of the application.
     * @param primaryStage The primary stage for this application.
     */
    @Override
    public void start(Stage primaryStage) {
        com.emr.gds.context.DataInitializer.initialize();
        this.startTime = System.currentTimeMillis();
        this.mainStage = primaryStage;
        primaryStage.setTitle(APP_TITLE);

        showLoginScene(primaryStage);
        logger.info("Time to first window: {}ms", (System.currentTimeMillis() - startTime));
    }

    private void showLoginScene(Stage stage) {
        Scene loginScene = buildLoginScene();
        stage.setScene(loginScene);
        stage.show();
    }

    private Scene buildLoginScene() {
        BorderPane shell = new BorderPane();
        shell.setPadding(new Insets(40, 32, 40, 32));
        shell.setStyle("-fx-background-color: linear-gradient(to bottom right, #0f172a, #1e293b);");

        VBox card = new VBox(14);
        card.setPadding(new Insets(24));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(440);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-background-radius: 18; -fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.35), 24, 0, 0, 12);");

        Label badge = new Label("GDSEMR");
        badge.setStyle("-fx-text-fill: #a5b4fc; -fx-font-size: 13px; -fx-font-weight: bold;");

        Label title = new Label("Sign in to continue");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        Label subtitle = new Label("Secure access to the EMR workspace.");
        subtitle.setStyle("-fx-text-fill: rgba(255,255,255,0.75); -fx-font-size: 14px;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(360);
        usernameField.setStyle("-fx-background-radius: 12; -fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.55);");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(360);
        passwordField.setStyle("-fx-background-radius: 12; -fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: white; -fx-prompt-text-fill: rgba(255,255,255,0.55);");

        usernameField.setOnAction(e -> passwordField.requestFocus());

        Button loginButton = new Button("Sign In");
        loginButton.setDefaultButton(true);
        loginButton.setPrefWidth(160);
        loginButton.setStyle("-fx-background-radius: 12; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: linear-gradient(to right, #4f46e5, #06b6d4);");

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #facc15; -fx-font-size: 12px;");

        loginButton.setOnAction(e -> handleLogin(usernameField, passwordField, statusLabel, loginButton));
        passwordField.setOnAction(e -> handleLogin(usernameField, passwordField, statusLabel, loginButton));

        VBox buttonRow = new VBox(loginButton);
        buttonRow.setAlignment(Pos.CENTER_LEFT);
        buttonRow.setPadding(new Insets(8, 0, 0, 0));

        card.getChildren().addAll(badge, title, subtitle, usernameField, passwordField, buttonRow, statusLabel);

        StackPane center = new StackPane(card);
        center.setAlignment(Pos.CENTER);
        shell.setCenter(center);

        return new Scene(shell, 900, 600);
    }

    private void handleLogin(TextField usernameField, PasswordField passwordField, Label statusLabel, Button loginButton) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Enter both username and password.");
            return;
        }

        statusLabel.setText("Signing in & loading data...");
        loginButton.setDisable(true);
        usernameField.setDisable(true);
        passwordField.setDisable(true);

        // Load data asynchronously
        javafx.concurrent.Task<Map<String, String>> loadTask = new javafx.concurrent.Task<>() {
            @Override
            protected Map<String, String> call() throws Exception {
                return AppContext.getInstance().getAbbreviationService().loadAll();
            }
        };

        loadTask.setOnSucceeded(e -> {
            launchMainScene(loadTask.getValue());
        });

        loadTask.setOnFailed(e -> {
            statusLabel.setText("Error loading data: " + loadTask.getException().getMessage());
            loginButton.setDisable(false);
            usernameField.setDisable(false);
            passwordField.setDisable(false);
            loadTask.getException().printStackTrace();
        });

        new Thread(loadTask).start();
    }

    private void launchMainScene(Map<String, String> loadedAbbreviations) {
        try {
            // Initialize core components before building the UI
            initializeApplicationComponents(loadedAbbreviations);
            
            // Build the main layout
            BorderPane root = buildRootLayout();
            root.getStyleClass().add("font-small");
            Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
            
            // Load the CSS stylesheet
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            
            mainStage.setTitle(APP_TITLE);
            mainStage.setScene(scene);
            mainStage.show();
            
            // Perform setup tasks after the stage is visible
            configurePostShow(scene);
            
            logger.info("Time to main scene: {}ms", (System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            logger.error("Failed to launch main scene", e);
            showFatalError("Application Startup Error", "Failed to start the application.", e);
        }
    }

    public Stage getPrimaryStage() {
        return mainStage;
    }

    /**
     * Cleans up resources when the application is closed.
     */
    @Override
    public void stop() throws Exception {
        super.stop();
        // Ensure background tasks and database connections are closed
        if (problemAction != null) {
            problemAction.closeResources();
        }
        AppDatabaseManager.getInstance().closeAll();
        logger.info("All database connections closed.");
    }

    // ================================
    // Initialization Methods
    // ================================

    /**
     * Initializes database connection and core application managers.
     */
    private void initializeApplicationComponents(Map<String, String> loadedAbbreviations) throws IOException, ClassNotFoundException, SQLException {
        AppContext context = AppContext.getInstance();
        
        problemAction = new IAMProblemAction(this, context.getProblemListService());
        textAreaManager = new IAMTextArea(context.getAbbrevMap(), problemAction, context.getAbbreviationService(), context.getPlanHistoryService());
        buttonAction = new IAMButtonAction(this, context.getAbbreviationService());
        textAreaManager.setAssessmentDoubleClickHandler((textArea, index) -> buttonAction.openKcd9Manager());
        functionKeyHandler = new IAMFunctionkey(this);
    }

    // ================================
    // UI Layout Methods
    // ================================

    /**
     * Constructs the root BorderPane layout for the main scene.
     */
    private BorderPane buildRootLayout() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        root.setTop(buildTopToolBar());
        root.setLeft(buildLeftPanel());
        root.setCenter(buildCenterPanel());
        root.setBottom(buildBottomPanel());

        // Establish the connection bridge for inter-component communication
        establishBridgeConnection();

        return root;
    }

    /**
     * Builds the top toolbar with action buttons.
     */
    private ToolBar buildTopToolBar() {
        ToolBar topBar = buttonAction.buildTopBar();

        // Create and configure additional buttons
        Button templateButton = new Button("Load Template");
        templateButton.setOnAction(e -> openTemplateEditor());

        Button vitalButton = new Button("Vital BP & HbA1c");
        vitalButton.setOnAction(e -> openVitalWindow());
        
        // --- Diagnostic Tools (Accented) ---
        Button dexaButton = new Button("DEXA");
        dexaButton.getStyleClass().add("button-accent");
        dexaButton.setOnAction(e -> {
            DexaRiskAssessmentApp.open();
        });
        
        Button ekgButton = new Button("EKG");
        ekgButton.getStyleClass().add("button-accent");
        ekgButton.setOnAction(e -> EkgReportStage.open());
        
        Button ekgQuickButton = new Button("EKG Quick");
        ekgQuickButton.getStyleClass().add("button-accent");
        ekgQuickButton.setOnAction(e -> EkgQuickInterpreter.open());
        
        Button cpaButton = new Button("Chest X-ray");
        cpaButton.getStyleClass().add("button-accent");
        cpaButton.setOnAction(event -> {
            ChestXrayReviewStage chestPAWindow = new ChestXrayReviewStage(mainStage);
            chestPAWindow.show();
        });

        Button categoryButton = new Button("Category");
        categoryButton.setOnAction(e -> {
            try {
                new MedicationCategory().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        // Add buttons to the toolbar
        topBar.getItems().addAll(
            new Separator(), templateButton,
            new Separator(), vitalButton,
            new Separator(), categoryButton,
            new Separator(), 
            new Label("Diagnostics:"), // Group Label
            dexaButton, ekgButton, ekgQuickButton, cpaButton
        );
        return topBar;
    }

    /**
     * Builds the left panel containing the problem list.
     */
    private VBox buildLeftPanel() {
        VBox leftPanel = problemAction.buildProblemPane();
        BorderPane.setMargin(leftPanel, new Insets(0, 10, 0, 0));
        return leftPanel;
    }

    /**
     * Builds the center panel with the main EMR text areas.
     */
    private GridPane buildCenterPanel() {
        GridPane centerPane = textAreaManager.buildCenterAreas();
        centerPane.setStyle("-fx-background-color: linear-gradient(to bottom right, #fdfbf7, #fbf8f1);"); // Ecru gradient
        return centerPane;
    }

    /**
     * Builds the bottom toolbar.
     */
    private ToolBar buildBottomPanel() {
        try {
            ToolBar bottomBar = buttonAction.buildBottomBar();

            Button categoryButton = new Button("Category");
            categoryButton.getStyleClass().add("button-accent");
            categoryButton.setOnAction(e -> {
                try {
                    new MedicationCategory().start(new Stage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            Button thyroidButton = new Button("Thyroid");
            thyroidButton.getStyleClass().add("button-accent");
            thyroidButton.setOnAction(e -> ThyroidLauncher.openThyroidEmr());

            Button thyroidPregButton = new Button("Thyroid Pregnancy");
            thyroidPregButton.getStyleClass().add("button-accent");
            thyroidPregButton.setOnAction(e -> ThyroidLauncher.openThyroidPregnancy());

            Button allergyButton = new Button("Allergy");
            allergyButton.getStyleClass().add("button-accent");
            allergyButton.setOnAction(e -> {
                try {
                    new AllergyApp().start(new Stage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            Button labCodeButton = new Button("LabCode");
            labCodeButton.getStyleClass().add("button-accent");
            labCodeButton.setOnAction(e -> com.emr.gds.features.clinicalLab.ClinicalLabLauncher.open());
            
            bottomBar.getItems().add(new Separator());
            bottomBar.getItems().add(categoryButton);
            bottomBar.getItems().add(new Separator());
            bottomBar.getItems().add(thyroidButton);
            bottomBar.getItems().add(thyroidPregButton);
            bottomBar.getItems().add(new Separator());
            bottomBar.getItems().add(allergyButton);
            bottomBar.getItems().add(new Separator());
            bottomBar.getItems().add(labCodeButton);
            bottomBar.getItems().add(new Separator());

            Button referenceButton = new Button("Reference");
            referenceButton.getStyleClass().add("button-accent");
            referenceButton.setOnAction(e -> openReferenceManager());

            bottomBar.getItems().add(referenceButton);
            
            return bottomBar;
        } catch (Exception e) {
            System.err.println("Error building bottom panel: " + e.getMessage());
            e.printStackTrace();
            // Provide a fallback UI in case of an error
            ToolBar fallbackToolBar = new ToolBar();
            Label errorLabel = new Label("Error loading bottom panel");
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
            fallbackToolBar.getItems().add(errorLabel);
            return fallbackToolBar;
        }
    }

    // ================================
    // Window Management
    // ================================

    /**
     * Opens or focuses the vital signs window.
     */
    public void openVitalWindow() {
        if (!isBridgeReady()) {
            showToast("Text areas not ready yet. Please try again in a moment.");
            return;
        }

        // Use a singleton pattern for the vital signs window
        if (freqStage == null || !freqStage.isShowing()) {
            freqStage = new IAIFreqFrame();
        } else {
            freqStage.requestFocus();
            freqStage.toFront();
        }
    }

    /**
     * Opens the EMR template editor.
     */
    private void openTemplateEditor() {
        com.emr.gds.features.template.TemplateEditStage.open(templateContent ->
            textAreaManager.parseAndAppendTemplate(templateContent)
        );
    }

    /**
     * Opens the Reference Manager window.
     */
    private void openReferenceManager() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reference_frame.fxml"));
            Stage referenceStage = new Stage();
            referenceStage.setTitle("Reference Manager");
            referenceStage.setScene(new Scene(loader.load()));
            
            // Get the controller and inject the base path and service
            com.emr.gds.features.ReferenceFile.ReferenceController controller = loader.getController();
            File referenceBasePath = RuntimeEnvironment.getDatabaseDirectory().resolve("references").toFile();
            controller.setBasePath(referenceBasePath);
            
            // Inject ReferenceService from AppContext
            controller.setReferenceService(AppContext.getInstance().getReferenceService());
            
            controller.initData();

            referenceStage.setResizable(false);
            referenceStage.show();
        } catch (IOException e) {
            showFatalError("Reference Manager Error", "Failed to open Reference Manager.", e);
        }
    }

    // ================================
    // Post-Initialization Setup
    // ================================

    /**
     * Configures the application after the main stage is shown.
     */
    private void configurePostShow(Scene scene) {
        TextAreaControlProcessor.installGlobalProcessor(AppContext.getInstance().getAbbrevMap());

        Platform.runLater(() -> {
            // Ensure the bridge is ready and set initial focus
            if (!isBridgeReady()) {
                establishBridgeConnection();
            }
            textAreaManager.focusArea(INITIAL_FOCUS_AREA);
        });
        installAllKeyboardShortcuts(scene);
    }

    /**
     * Establishes a static bridge to allow external components (like Swing windows)
     * to interact with the JavaFX text areas.
     */
    private void establishBridgeConnection() {
        var areas = textAreaManager.getInternalTextAreas();
        if (areas == null || areas.isEmpty()) {
            throw new IllegalStateException("EMR text areas not initialized. buildCenterAreas() must run first.");
        }
        // Set the global static manager for external access
        IAIMain.setTextAreaManager(new IAIFxTextAreaManager(areas));
    }

    /**
     * Checks if the text area bridge is ready for interaction.
     */
    private boolean isBridgeReady() {
        return Optional.ofNullable(IAIMain.getTextAreaManager())
                       .map(IAITextAreaManager::isReady)
                       .orElse(false);
    }

    // ================================
    // Keyboard Shortcuts
    // ================================

    /**
     * Installs all keyboard shortcuts for the application.
     */
    private void installAllKeyboardShortcuts(Scene scene) {
        installGlobalKeyboardShortcuts(scene);
        functionKeyHandler.installFunctionKeyShortcuts(scene);
    }

    /**
     * Installs global shortcuts like date insertion, formatting, and copying.
     */
    private void installGlobalKeyboardShortcuts(Scene scene) {
        Map<KeyCombination, Runnable> shortcuts = new HashMap<>();

        // Ctrl+I: Insert current date
        shortcuts.put(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN), () -> {
            String currentDateString = " [ " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " ]";
            insertLineIntoFocusedArea(currentDateString);
        });
        
        // Ctrl+Shift+F: Format current text area
        shortcuts.put(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN), this::formatCurrentArea);
        
        // Ctrl+Shift+C: Copy all content to clipboard
        shortcuts.put(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN), this::copyAllToClipboard);

        addAreaFocusShortcuts(shortcuts);

        // Register all shortcuts with the scene
        shortcuts.forEach((keyCombination, action) -> scene.getAccelerators().put(keyCombination, action));
    }

    /**
     * Adds shortcuts (Ctrl+1 through Ctrl+0) to focus specific text areas.
     */
    private void addAreaFocusShortcuts(Map<KeyCombination, Runnable> shortcuts) {
        for (int i = 1; i <= 9; i++) {
            final int areaIndex = i - 1;
            shortcuts.put(new KeyCodeCombination(KeyCode.getKeyCode(String.valueOf(i)), KeyCombination.CONTROL_DOWN),
                          () -> textAreaManager.focusArea(areaIndex));
        }
        // Ctrl+0 focuses the 10th area
        shortcuts.put(new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.CONTROL_DOWN), () -> textAreaManager.focusArea(9));
    }

    // ================================
    // Text Manipulation Methods
    // ================================

    public void insertTemplateIntoFocusedArea(IAMButtonAction.TemplateLibrary template) {
        textAreaManager.insertTemplateIntoFocusedArea(template);
    }

    public void insertLineIntoFocusedArea(String line) {
        textAreaManager.insertLineIntoFocusedArea(line);
    }

    public void insertBlockIntoFocusedArea(String block) {
        textAreaManager.insertBlockIntoFocusedArea(block);
    }

    public void formatCurrentArea() {
        textAreaManager.formatCurrentArea();
    }

    public void clearAllText() {
        textAreaManager.clearAllTextAreas();
        Optional.ofNullable(problemAction).ifPresent(IAMProblemAction::clearScratchpad);
    }

    // ================================
    // Clipboard Operations
    // ================================

    /**
     * Compiles all EMR content, formats it, and copies it to the system clipboard.
     */
    public void copyAllToClipboard() {
        String compiledContent = compileAllContent();
        String finalizedContent = IAMTextFormatUtil.finalizeForEMR(compiledContent);

        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(finalizedContent);
        Clipboard.getSystemClipboard().setContent(clipboardContent);

        showToast("Copied all content to clipboard");
    }

    /**
     * Gathers content from the scratchpad.
     */
    private String compileAllContent() {
        return Optional.ofNullable(problemAction)
                       .map(IAMProblemAction::getScratchpadText)
                       .orElse("");
    }

    // ================================
    // Utility Methods
    // ================================

    /**
     * Finds the root directory of the repository.
     */
    private Path getRepoRoot() {
        Path p = Paths.get("").toAbsolutePath();
        // Traverse up until a marker file is found
        while (p != null && !Files.exists(p.resolve("gradlew")) && !Files.exists(p.resolve(".git"))) {
            p = p.getParent();
        }
        return (p != null) ? p : Paths.get("").toAbsolutePath();
    }

    /**
     * Constructs the full path to a database file within the project structure.
     */
    private Path getDbPath(String fileName) {
        return getRepoRoot().resolve("app").resolve("db").resolve(fileName);
    }

    /**
     * Displays a simple informational pop-up message.
     */
    private void showToast(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle("Info");
        alert.showAndWait();
    }

    /**
     * Displays a fatal error message and exits the application.
     */
    private void showFatalError(String title, String message, Throwable cause) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("A fatal error occurred.");
        alert.setContentText(message + "\n\nDetails: " + cause.getMessage());
        
        // Add expandable stack trace
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        cause.printStackTrace(pw);
        TextArea textArea = new TextArea(sw.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        alert.getDialogPane().setExpandableContent(textArea);
        
        alert.showAndWait();
        Platform.exit();
    }

    // ================================
    // Getters for Component Access
    // ================================

    public void applyTextAreaTheme(IAMTextArea.Theme theme) {
        if (textAreaManager != null) {
            textAreaManager.setTheme(theme);
        }
    }

    public IAMTextArea getTextAreaManager() {
        return textAreaManager;
    }

    public Map<String, String> getAbbrevMap() {
        return AppContext.getInstance().getAbbrevMap();
    }

    public IAMFunctionkey getFunctionKeyHandler() {
        return functionKeyHandler;
    }
}
