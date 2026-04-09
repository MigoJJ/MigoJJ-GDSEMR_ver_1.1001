package com.emr.gds.shared.ui;

import com.emr.gds.IttiaApp;
import com.emr.gds.service.ProblemListService;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Manages the Problem List and Scratchpad sections of the UI.
 * This class handles:
 * - Displaying and managing a persistent list of patient problems.
 * - Storing and retrieving problem data from a dedicated SQLite database.
 * - Providing a scratchpad area that mirrors content from the main EMR text areas.
 */
public class IAMProblemAction {

    // ================================ 
    // UI Layout Constants
    // ================================ 
    private static final double SIDEBAR_WIDTH_PX = 460;
    private static final int SCRATCHPAD_ROWS = 35;
    private static final double PANEL_HEIGHT_PX = 600;
    private static final double SCRATCHPAD_RATIO = 0.6; // Scratchpad : Problem List = 6 : 4
    private static final double PROBLIST_HEIGHT_PX = PANEL_HEIGHT_PX * (1 - SCRATCHPAD_RATIO);
    private static final double SPACING_PX = 8;
    private static final double PADDING_RIGHT_PX = 8;

    // ================================ 
    // Instance Variables
    // ================================ 
    private final IttiaApp app;
    private final ProblemListService problemListService;
    private final ObservableList<String> problems = FXCollections.observableArrayList();
    private final Comparator<String> problemComparator = String::compareToIgnoreCase;
    private ListView<String> problemList;
    private Button saveToEmrButton;
    private TextArea scratchpadArea;
    private final LinkedHashMap<String, String> scratchpadEntries = new LinkedHashMap<>();

    // ================================ 
    // Constructor
    // ================================ 
    public IAMProblemAction(IttiaApp app, ProblemListService problemListService) {
        this.app = app;
        this.problemListService = problemListService;
        initProblemListStore();
        loadProblemsFromStore();
    }

    // ================================ 
    // Database Initialization and Operations
    // ================================ 

    /**
     * Initializes the connection to the 'prolist.db' SQLite database.
     * Creates the database and table if they don't exist.
     */
    private void initProblemListStore() {
        try {
            problemListService.initialize();
        } catch (SQLException e) {
            System.err.println("FATAL: Failed to initialize Problem List store: " + e.getMessage());
            throw new RuntimeException("Failed to initialize problem list store", e);
        }
    }

    /**
     * Loads all problems from the database into the UI's ObservableList.
     */
    private void loadProblemsFromStore() {
        problems.clear();
        try {
            problems.addAll(problemListService.loadAll());
        } catch (SQLException e) {
            System.err.println("Failed to load problems from database: " + e.getMessage());
        }
    }

    /**
     * Adds a new problem to the database and updates the UI.
     * @param problemText The problem to add.
     */
    private void addProblem(String problemText) {
        if (problemText == null || problemText.isBlank()) return;

        try {
            problemListService.add(problemText);
            Platform.runLater(() -> problems.add(problemText));
        } catch (SQLException e) {
            // This error is expected if the problem already exists due to the UNIQUE constraint.
            System.err.println("Failed to add problem '" + problemText + "'. It might already exist. Details: " + e.getMessage());
        }
    }

    /**
     * Removes a selected problem from the database and updates the UI.
     * @param problemText The problem to remove.
     */
    private void removeProblem(String problemText) {
        if (problemText == null) return;

        try {
            if (problemListService.delete(problemText)) {
                Platform.runLater(() -> problems.remove(problemText));
            }
        } catch (SQLException e) {
            System.err.println("Failed to remove problem '" + problemText + "': " + e.getMessage());
        }
    }

    // ================================ 
    // UI Building
    // ================================ 

    /**
     * Constructs the entire problem pane, including the problem list and scratchpad.
     * @return A VBox containing the configured UI components.
     */
    public VBox buildProblemPane() {
        // --- Problem List Section ---
        problemList = createProblemListView();
        TextField input = createProblemInputTextField();
        saveToEmrButton = createSaveToEmrButton();
        Button removeButton = createRemoveProblemButton();
        HBox problemControls = new HBox(SPACING_PX, input, saveToEmrButton, removeButton);
        HBox.setHgrow(input, Priority.ALWAYS);
        problemList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> updateSaveButtonState(newV));
        updateSaveButtonState(problemList.getSelectionModel().getSelectedItem());

        // --- Scratchpad Section ---
        scratchpadArea = createScratchpadTextArea();

        // --- Assemble the VBox ---
        VBox box = new VBox(
                SPACING_PX,
                new Label("Scratchpad"),
                scratchpadArea,
                new Separator(Orientation.HORIZONTAL),
                new Label("Problem List (Persistent)"),
                problemList,
                problemControls
        );

        // Configure layout properties
        VBox.setVgrow(problemList, Priority.ALWAYS);
        VBox.setVgrow(scratchpadArea, Priority.ALWAYS);
        box.setPadding(new Insets(0, PADDING_RIGHT_PX, 0, 0));
        box.setPrefWidth(SIDEBAR_WIDTH_PX);
        box.setMaxWidth(SIDEBAR_WIDTH_PX);
        box.setMinWidth(SIDEBAR_WIDTH_PX);

        return box;
    }

    private ListView<String> createProblemListView() {
        SortedList<String> sortedProblems = new SortedList<>(problems, problemComparator);
        ListView<String> listView = new ListView<>(sortedProblems);
        listView.setPrefHeight(PROBLIST_HEIGHT_PX);
        listView.setMinHeight(PROBLIST_HEIGHT_PX * 0.8);
        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String selectedItem = listView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    app.insertLineIntoFocusedArea("- " + selectedItem);
                }
            }
        });
        return listView;
    }

    private Button createSaveToEmrButton() {
        Button b = new Button("Save to EMR");
        b.setOnAction(e -> {
            String selectedItem = problemList.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                app.insertLineIntoFocusedArea("- " + selectedItem);
            }
        });
        b.setDisable(true);
        return b;
    }

    private void updateSaveButtonState(String selectedItem) {
        if (saveToEmrButton != null) {
            saveToEmrButton.setDisable(selectedItem == null);
        }
    }

    private TextField createProblemInputTextField() {
        TextField input = new TextField();
        input.setPromptText("Add problem and press Enter");
        input.setOnAction(e -> {
            String text = IAMTextFormatUtil.normalizeLine(input.getText());
            if (!text.isBlank()) {
                addProblem(text); // Persist to DB and update UI
                input.clear();
            }
        });
        return input;
    }

    private Button createRemoveProblemButton() {
        Button remove = new Button("Remove Selected");
        remove.setOnAction(e -> {
            String selectedProblem = problemList.getSelectionModel().getSelectedItem();
            if (selectedProblem != null) {
                removeProblem(selectedProblem);
            }
        });
        return remove;
    }

    private TextArea createScratchpadTextArea() {
        TextArea textArea = new TextArea();
        textArea.setStyle("-fx-font-size: 13px;"); // Default to small
        textArea.setPromptText("Scratchpad... (auto-updated from center areas)");
        textArea.setWrapText(true);
        textArea.setEditable(true);
        textArea.setPrefRowCount(SCRATCHPAD_ROWS);
        textArea.setPrefHeight(PANEL_HEIGHT_PX * SCRATCHPAD_RATIO);
        textArea.setMinHeight(PANEL_HEIGHT_PX * SCRATCHPAD_RATIO * 0.8);
        return textArea;
    }

    // ================================ 
    // Scratchpad Logic
    // ================================ 

    /**
     * Updates the scratchpad content based on changes in the main text areas.
     * @param title The title of the text area that changed.
     * @param newText The new text content.
     */
    public void updateAndRedrawScratchpad(String title, String newText) {
        if (newText.isEmpty()) {
            scratchpadEntries.remove(title);
        } else {
            scratchpadEntries.put(title, newText);
        }
        redrawScratchpad();
    }

    /**
     * Redraws the scratchpad with the latest content from all mirrored text areas.
     */
    public void redrawScratchpad() {
        if (scratchpadArea == null) return;

        List<String> orderedTitles = Arrays.asList(IAMTextArea.TEXT_AREA_TITLES);
        StringJoiner sj = new StringJoiner("\n");

        for (String title : orderedTitles) {
            String value = scratchpadEntries.get(title);
            if (value != null && !value.isEmpty()) {
                String filteredValue = value.lines()
                                            .filter(line -> !line.isBlank())
                                            .collect(Collectors.joining("\n"));
                if (!filteredValue.isEmpty()) {
                    sj.add(title + "\n" + filteredValue);
                }
            }
        }

        String newContent = sj.toString();
        if (!scratchpadArea.getText().equals(newContent)) {
            scratchpadArea.setText(newContent);
            // Let user control their view
            // scratchpadArea.positionCaret(scratchpadArea.getLength());
            // scratchpadArea.setScrollTop(Double.MAX_VALUE);
        }
    }

    public void clearScratchpad() {
        if (scratchpadArea != null) {
            scratchpadArea.clear();
        }
    }

    // ================================ 
    // Public Getters and Cleanup
    // ================================ 

    public ObservableList<String> getProblems() {
        return problems;
    }

    /**
     * Backwards-compatible wrapper to close resources.
     */
    public void closeResources() {
        // no-op: repositories use short-lived connections
    }
}
