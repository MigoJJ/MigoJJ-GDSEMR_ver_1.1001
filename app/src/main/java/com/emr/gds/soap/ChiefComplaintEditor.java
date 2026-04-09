package com.emr.gds.soap;

import com.emr.gds.service.AbbreviationService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * A specialized editor for the Chief Complaint section, featuring quick templates, a phrase bank,
 * and automatic abbreviation expansion.
 */
public class ChiefComplaintEditor {

    private final TextArea sourceTextArea;
    private Stage editorStage;
    private TextArea editorTextArea;
    private final Map<String, String> abbrevMap;

    private final String[] ccTemplates = {
            "Chest pain", "Shortness of breath", "Abdominal pain", "Headache", "Back pain",
            "Nausea and vomiting", "Fever", "Cough", "Dizziness", "Fatigue", "Vertigo",
            "Palpitation", "Dysuria", "Diarrhea", "Constipation",
            "Acute", "Chronic", "Severe", "Persistent", "Intermittent", "Localized",
            "Radiating", "Progressive", "Recurrent", "Generalized", "Exacerbated",
            "Associated", "Episodic", "Subacute", "Relieved",
            "[ :cd ]", "-day ago onset", "-week ago onset", "-month ago onset", "-year ago onset"
    };

    private final String[] clinicalPhrases = {
            "Patient presents with acute onset of severe chest pain radiating to the left arm.",
            "Complains of persistent shortness of breath worsened by physical activity.",
            "Reports moderate to severe abdominal pain localized to the right lower quadrant.",
            "Experiencing high fever with chills and night sweats for several days.",
            "Presents with a throbbing headache accompanied by nausea and light sensitivity."
    };

    public static class Phrase {
        private final SimpleStringProperty text;
        public Phrase(String text) { this.text = new SimpleStringProperty(text); } 
        public String getText() { return text.get(); }
    }

    public ChiefComplaintEditor(TextArea sourceTextArea, AbbreviationService abbreviationService) {
        this.sourceTextArea = sourceTextArea;
        this.abbrevMap = abbreviationService.getAbbreviations();
        createEditorWindow();
    }

    private void createEditorWindow() {
        editorStage = new Stage();
        editorStage.setTitle("Chief Complaint Editor");
        editorStage.initModality(Modality.APPLICATION_MODAL);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setTop(createTopSection());
        root.setCenter(createCenterSection());
        root.setBottom(createBottomSection());
        root.setLeft(createWestPanel(320));

        editorStage.setScene(new Scene(root, 1100, 600));
    }

    private VBox createWestPanel(double width) {
        TableView<Phrase> table = new TableView<>();
        table.setPrefWidth(width);
        TableColumn<Phrase, String> phraseColumn = new TableColumn<>("Clinical Phrases");
        phraseColumn.setCellValueFactory(new PropertyValueFactory<>("text"));
        phraseColumn.prefWidthProperty().bind(table.widthProperty());
        table.getColumns().add(phraseColumn);

        ObservableList<Phrase> phraseData = Arrays.stream(clinicalPhrases).map(Phrase::new).collect(Collectors.toCollection(FXCollections::observableArrayList));
        table.setItems(phraseData);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, newSelection) -> {
            if (newSelection != null) {
                editorTextArea.appendText(newSelection.getText() + " ");
                editorTextArea.requestFocus();
            }
        });

        return new VBox(5, createStyledLabel("Phrase Bank", "-fx-font-weight: bold;"), table);
    }

    private VBox createTopSection() {
        return new VBox(5,
                createStyledLabel("Chief Complaint Editor", "-fx-font-size: 16px; -fx-font-weight: bold;"),
                createStyledLabel("Edit the patient's chief complaint. Use templates or the phrase bank.", "-fx-text-fill: #666;"),
                createStyledLabel("Tip: Use abbreviations like ':cd' for the current date.", "-fx-font-size: 11px; -fx-font-style: italic; -fx-text-fill: #0066cc;")
        );
    }

    private VBox createCenterSection() {
        editorTextArea = new TextArea(sourceTextArea.getText());
        editorTextArea.setWrapText(true);
        editorTextArea.setPrefRowCount(10);

        TextArea previewArea = new TextArea();
        previewArea.setEditable(false);
        previewArea.setWrapText(true);
        previewArea.setPrefRowCount(4);
        previewArea.setStyle("-fx-background-color: #f5f5f5;");

        editorTextArea.textProperty().addListener((obs, old, newText) -> previewArea.setText(expandAbbreviations(newText)));
        previewArea.setText(expandAbbreviations(editorTextArea.getText()));

        return new VBox(10,
                createStyledLabel("Quick Templates:", "-fx-font-weight: bold;"),
                createTemplatesGrid(),
                createStyledLabel("Chief Complaint Text:", "-fx-font-weight: bold;"),
                editorTextArea,
                createStyledLabel("Preview (Expanded):", "-fx-font-weight: bold; -fx-text-fill: #666;"),
                previewArea
        );
    }

    private GridPane createTemplatesGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        int cols = 5;
        for (int i = 0; i < ccTemplates.length; i++) {
            String template = ccTemplates[i];
            Button btn = new Button(template);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setOnAction(e -> editorTextArea.appendText(template + " "));
            grid.add(btn, i % cols, i / cols);
        }
        return grid;
    }

    private HBox createBottomSection() {
        Button applyButton = createStyledButton("Apply Changes", "-fx-background-color: #4CAF50; -fx-text-fill: white;", e -> applyChanges());
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> editorStage.close());
        Button clearButton = createStyledButton("Clear", "-fx-background-color: #f44336; -fx-text-fill: white;", e -> editorTextArea.clear());

        HBox bottomBar = new HBox(10, applyButton, cancelButton, clearButton);
        bottomBar.setPadding(new Insets(10, 0, 0, 0));
        return bottomBar;
    }

    private void applyChanges() {
        String originalText = editorTextArea.getText();
        String expandedText = expandAbbreviations(originalText);

        if (!originalText.equals(expandedText)) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Abbreviation Expansion");
            confirm.setHeaderText("Apply expanded text?");
            confirm.setContentText("Original: " + originalText + "\n\nExpanded: " + expandedText);
            if (confirm.showAndWait().filter(b -> b == ButtonType.OK).isEmpty()) {
                return; // User cancelled
            }
        }
        sourceTextArea.setText(expandedText);
        editorStage.close();
    }

    private String expandAbbreviations(String text) {
        return Arrays.stream(text.split("((?<= )|(?= ))"))
                .map(word -> {
                    String cleanWord = word.trim();
                    if (":cd".equals(cleanWord)) return LocalDate.now().format(DateTimeFormatter.ISO_DATE);
                    return cleanWord.startsWith(":") ? abbrevMap.getOrDefault(cleanWord.substring(1), word) : word;
                })
                .collect(Collectors.joining());
    }

    private Label createStyledLabel(String text, String style) {
        Label label = new Label(text);
        label.setStyle(style);
        return label;
    }

    private Button createStyledButton(String text, String style, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button button = new Button(text);
        button.setStyle(style);
        button.setOnAction(handler);
        return button;
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }

    public void showAndWait() {
        editorStage.showAndWait();
    }
}
