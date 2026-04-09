package com.emr.gds.soap.view;

import com.emr.gds.util.I18N;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.Node;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;

public class PMHView {

    private GridPane mainGrid;
    private VBox footerBox;
    private TextArea outputArea;

    // Buttons exposed to Presenter
    private Button btnSave;
    private Button btnDefault;
    private Button btnClear;
    private Button btnCopy;
    private Button btnFMH;
    private Button btnQuit;

    private final Font defaultFont = Font.font("Segoe UI", 11);
    private final Font outputFont = Font.font("Consolas", 12);

    // List to maintain traversal order for accessibility
    private final List<Node> traversalOrderNodes = new ArrayList<>();
    // Map to store TextAreas by category name for direct access from Presenter
    private final Map<String, TextArea> categoryToTextAreaMap = new HashMap<>();

    public PMHView() {
        // These are initialized by the presenter, this constructor can remain minimal.
    }

    public BorderPane buildView(int numColumns) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.getStyleClass().add("pmh-root");

        Label title = new Label(I18N.get("pmh.title"));
        title.setFont(Font.font("Segoe UI", 17));
        title.setPadding(new Insets(0, 0, 15, 0));
        title.getStyleClass().add("pmh-title");
        root.setTop(title);

        mainGrid = createGrid(numColumns);
        ScrollPane scroller = new ScrollPane(mainGrid);
        scroller.setFitToWidth(true);
        scroller.getStyleClass().add("pmh-scroll");
        root.setCenter(scroller);

        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefRowCount(8);
        outputArea.setWrapText(false);
        outputArea.setFont(outputFont);
        outputArea.setPromptText(I18N.get("pmh.output.prompt"));
        outputArea.getStyleClass().add("pmh-output");
        
        footerBox = buildFooter();
        root.setBottom(footerBox);
        
        return root;
    }

    private GridPane createGrid(int numColumns) {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));
        grid.getStyleClass().add("pmh-grid");

        for (int i = 0; i < numColumns; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / numColumns);
            grid.getColumnConstraints().add(col);
        }
        return grid;
    }

    // Modified to accept categoryName and store TextArea
    public void addCategoryControl(String categoryName, CheckBox cb, TextArea ta, int row, int col) {
        VBox cellBox = new VBox(6, cb, ta);
        VBox.setVgrow(ta, Priority.ALWAYS);
        cellBox.getStyleClass().add("pmh-cell");
        mainGrid.add(cellBox, col, row);

        // Accessibility: Screen reader support
        cb.setAccessibleText(I18N.get("pmh.checkbox.accessible_text_prefix") + cb.getText());
        ta.setAccessibleText(I18N.get("pmh.textarea.accessible_text_prefix") + cb.getText());
        ta.setAccessibleHelp(I18N.get("pmh.textarea.accessible_help"));

        // Add to traversal order list
        traversalOrderNodes.add(cb);
        traversalOrderNodes.add(ta);
        // Store TextArea by category name
        categoryToTextAreaMap.put(categoryName, ta);
    }

    private VBox buildFooter() {
        btnSave = new Button(I18N.get("pmh.button.save"));
        btnDefault = new Button(I18N.get("pmh.button.default"));
        btnClear = new Button(I18N.get("pmh.button.clear"));
        btnCopy = new Button(I18N.get("pmh.button.copy"));
        btnFMH = new Button(I18N.get("pmh.button.open_emrfmh"));
        btnQuit = new Button(I18N.get("pmh.button.quit"));

        List.of(btnSave, btnDefault, btnClear, btnCopy, btnFMH, btnQuit).forEach(btn -> {
            btn.setFont(defaultFont);
            btn.getStyleClass().add("pmh-btn");
        });
        
        btnSave.getStyleClass().add("btn-save");
        btnDefault.getStyleClass().add("btn-default");
        btnClear.getStyleClass().add("btn-clear");
        btnCopy.getStyleClass().add("btn-copy");
        btnFMH.getStyleClass().add("btn-fmh");
        btnQuit.getStyleClass().add("btn-quit");

        HBox buttons = new HBox(10, btnSave, btnDefault, btnClear, btnCopy, btnFMH, btnQuit);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(10, 0, 0, 0));

        // Add buttons to traversal order
        traversalOrderNodes.addAll(buttons.getChildren());
        traversalOrderNodes.add(outputArea);

        return new VBox(8, new Separator(), outputArea, buttons);
    }
    
    public void applySceneStyling(Scene scene) {
        ThemeManager themeManager = new ThemeManager();
        themeManager.applyTheme(scene, ThemeManager.Theme.CLINICAL);
    }

    public void setupTraversalOrder(Scene scene) {
        List<Node> allTraversableNodes = new ArrayList<>();
        allTraversableNodes.add(scene.getRoot().lookup(".pmh-title"));
        allTraversableNodes.addAll(traversalOrderNodes);
        allTraversableNodes.removeIf(Objects::isNull); 
    }

    // New method to highlight/unhighlight TextArea
    public void highlightTextArea(String categoryName, boolean highlight) {
        TextArea ta = categoryToTextAreaMap.get(categoryName);
        if (ta != null) {
            if (highlight) {
                if (!ta.getStyleClass().contains("text-area-highlighted")) {
                    ta.getStyleClass().add("text-area-highlighted");
                }
            } else {
                ta.getStyleClass().remove("text-area-highlighted");
            }
        }
    }

    // --- Getters for Presenter to access UI elements ---
    public TextArea getOutputArea() {
        return outputArea;
    }

    public Button getBtnSave() {
        return btnSave;
    }

    public Button getBtnDefault() {
        return btnDefault;
    }

    public Button getBtnClear() {
        return btnClear;
    }

    public Button getBtnCopy() {
        return btnCopy;
    }

    public Button getBtnFMH() {
        return btnFMH;
    }

    public Button getBtnQuit() {
        return btnQuit;
    }

    public Font getDefaultFont() {
        return defaultFont;
    }
}
