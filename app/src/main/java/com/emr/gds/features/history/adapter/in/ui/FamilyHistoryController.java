package com.emr.gds.features.history.adapter.in.ui;

import com.emr.gds.features.history.application.FamilyHistoryService;
import com.emr.gds.features.history.domain.ConditionCategory;
import com.emr.gds.features.history.domain.HistoryPersistenceException;
import com.emr.gds.input.IAITextAreaManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

public class FamilyHistoryController {

    @FXML private ComboBox<String> relationshipComboBox;
    @FXML private TextArea notesTextArea;
    
    @FXML private ListView<String> endocrineList;
    @FXML private ListView<String> cancerList;
    @FXML private ListView<String> cardioList;
    @FXML private ListView<String> geneticList;
    
    @FXML private TextField searchField;
    @FXML private TextArea historyTextArea;

    private IAITextAreaManager textAreaManager;
    private Map<String, String> abbrevMap;
    private FamilyHistoryService service;

    private ObservableList<String> endocrineData = FXCollections.observableArrayList();
    private ObservableList<String> cancerData = FXCollections.observableArrayList();
    private ObservableList<String> cardioData = FXCollections.observableArrayList();
    private ObservableList<String> geneticData = FXCollections.observableArrayList();
    private TargetSelection lastFocusedSelection;

    public void setManagers(IAITextAreaManager manager, Map<String, String> abbrevMap, FamilyHistoryService service) {
        this.textAreaManager = manager;
        this.abbrevMap = (abbrevMap != null) ? abbrevMap : Collections.emptyMap();
        this.service = service;
        loadAllConditions();
    }

    @FXML
    public void initialize() {
        // Init ComboBox
        relationshipComboBox.setItems(FXCollections.observableArrayList(
                "Mother", "Father", "Sister", "Brother",
                "Grandmother", "Grandfather", "Aunt", "Uncle", "Cousin", "Child"
        ));

        // Bind Lists
        setupListView(endocrineList, endocrineData, ConditionCategory.ENDOCRINE);
        setupListView(cancerList, cancerData, ConditionCategory.CANCER);
        setupListView(cardioList, cardioData, ConditionCategory.CARDIOVASCULAR);
        setupListView(geneticList, geneticData, ConditionCategory.GENETIC);
        trackFocus(endocrineList, ConditionCategory.ENDOCRINE);
        trackFocus(cancerList, ConditionCategory.CANCER);
        trackFocus(cardioList, ConditionCategory.CARDIOVASCULAR);
        trackFocus(geneticList, ConditionCategory.GENETIC);

        // Search
        searchField.textProperty().addListener((obs, old, val) -> filterLists(val));
        
        // Abbreviation expansion
        setupAbbreviation(notesTextArea);
        setupAbbreviation(historyTextArea);
    }

    private void setupListView(ListView<String> listView, ObservableList<String> data, ConditionCategory category) {
        FilteredList<String> filtered = new FilteredList<>(data, p -> true);
        listView.setItems(filtered);
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.setContextMenu(createContextMenu(listView, category));
    }

    private ContextMenu createContextMenu(ListView<String> listView, ConditionCategory category) {
        ContextMenu menu = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleEdit(selected, category, listView);
            }
        });

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleDelete(selected, category, listView);
            }
        });
        menu.getItems().addAll(editItem, deleteItem);
        return menu;
    }

    private void trackFocus(ListView<String> listView, ConditionCategory category) {
        listView.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) {
                lastFocusedSelection = new TargetSelection(listView, category);
            }
        });
    }

    private void handleEdit(String oldName, ConditionCategory category, ListView<String> listView) {
        TextInputDialog dialog = new TextInputDialog(oldName);
        dialog.setTitle("Edit Condition");
        dialog.setHeaderText("Edit condition in " + category);
        dialog.setContentText("Name:");

        dialog.showAndWait().ifPresent(newName -> {
            String trimmed = newName.trim();
            if (trimmed.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Input Required", "Condition name cannot be empty.");
                return;
            }
            if (trimmed.equals(oldName)) {
                return;
            }
            if (isDuplicateName(listView, trimmed, oldName)) {
                showAlert(Alert.AlertType.WARNING, "Duplicate", "Condition already exists.");
                return;
            }
            if (!trimmed.equals(oldName)) {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() {
                        service.updateCondition(category, oldName, trimmed);
                        return null;
                    }
                };
                task.setOnSucceeded(e -> refreshList(category, listView));
                task.setOnFailed(e -> showAlert(Alert.AlertType.ERROR, "Update Failed", task.getException().getMessage()));
                new Thread(task).start();
            }
        });
    }

    private void handleDelete(String name, ConditionCategory category, ListView<String> listView) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete '" + name + "'?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("Confirm Deletion");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() {
                        service.deleteCondition(category, name);
                        return null;
                    }
                };
                task.setOnSucceeded(e -> refreshList(category, listView));
                task.setOnFailed(e -> showAlert(Alert.AlertType.ERROR, "Delete Failed", task.getException().getMessage()));
                new Thread(task).start();
            }
        });
    }

    private void refreshList(ConditionCategory category, ListView<String> listView) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                var updated = service.getConditions(category);
                Platform.runLater(() -> {
                    FilteredList<String> fl = (FilteredList<String>) listView.getItems();
                    @SuppressWarnings("unchecked")
                    ObservableList<String> source = (ObservableList<String>) fl.getSource();
                    source.setAll(updated);
                });
                return null;
            }
        };
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            showAlert(Alert.AlertType.ERROR, "Refresh Error", "Failed to refresh list: " + ex.getMessage());
        });
        new Thread(task).start();
    }

    private void loadAllConditions() {
        if (service == null) return;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                var endocrine = service.getConditions(ConditionCategory.ENDOCRINE);
                var cancer = service.getConditions(ConditionCategory.CANCER);
                var cardio = service.getConditions(ConditionCategory.CARDIOVASCULAR);
                var genetic = service.getConditions(ConditionCategory.GENETIC);

                Platform.runLater(() -> {
                    endocrineData.setAll(endocrine);
                    cancerData.setAll(cancer);
                    cardioData.setAll(cardio);
                    geneticData.setAll(genetic);
                });
                return null;
            }
        };
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            showAlert(Alert.AlertType.ERROR, "Data Load Error", "Failed to load conditions: " + ex.getMessage());
        });
        new Thread(task).start();
    }

    private void filterLists(String filter) {
        String lower = (filter == null) ? "" : filter.toLowerCase();
        updateFilter(endocrineList, lower);
        updateFilter(cancerList, lower);
        updateFilter(cardioList, lower);
        updateFilter(geneticList, lower);
    }

    @SuppressWarnings("unchecked")
    private void updateFilter(ListView<String> listView, String filter) {
        FilteredList<String> fl = (FilteredList<String>) listView.getItems();
        fl.setPredicate(s -> s.toLowerCase().contains(filter));
    }

    @FXML
    private void handleAddEntry() {
        String relationship = relationshipComboBox.getValue();
        if (relationship == null || relationship.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Input Required", "Please select a relationship.");
            return;
        }

        StringBuilder entry = new StringBuilder();
        entry.append(relationship).append(":\n");

        String notes = notesTextArea.getText().trim();
        if (!notes.isEmpty()) {
            entry.append("  Notes: ").append(notes).append("\n");
        }

        boolean hasCondition = false;
        hasCondition |= appendSelected(entry, ConditionCategory.ENDOCRINE.getLabel(), endocrineList);
        hasCondition |= appendSelected(entry, ConditionCategory.CANCER.getLabel(), cancerList);
        hasCondition |= appendSelected(entry, ConditionCategory.CARDIOVASCULAR.getLabel(), cardioList);
        hasCondition |= appendSelected(entry, ConditionCategory.GENETIC.getLabel(), geneticList);

        if (!hasCondition && notes.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Data", "Please select at least one condition or add notes.");
            return;
        }

        historyTextArea.appendText(entry.toString().trim() + "\n\n");
        clearFormInputs();
    }

    private boolean appendSelected(StringBuilder sb, String title, ListView<String> listView) {
        ObservableList<String> selected = listView.getSelectionModel().getSelectedItems();
        if (!selected.isEmpty()) {
            sb.append("  ").append(title).append(": ")
              .append(String.join("; ", selected)).append("\n");
            return true;
        }
        return false;
    }

    private void clearFormInputs() {
        relationshipComboBox.setValue(null);
        notesTextArea.clear();
        endocrineList.getSelectionModel().clearSelection();
        cancerList.getSelectionModel().clearSelection();
        cardioList.getSelectionModel().clearSelection();
        geneticList.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleAddCondition() {
        if (service == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "History service not available.");
            return;
        }
        
        TargetSelection selection = getTargetSelection();
        if (selection == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Click on a condition list first (to give it focus).");
            return;
        }
        
        ConditionCategory category = selection.category;
        ListView<String> target = selection.listView;
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add New Condition");
        dialog.setHeaderText("Add to " + category);
        dialog.setContentText("Condition name:");
        
        dialog.showAndWait().ifPresent(name -> {
            String trimmed = name.trim();
            if (trimmed.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Input Required", "Condition name cannot be empty.");
                return;
            }
            if (isDuplicateName(target, trimmed, null)) {
                showAlert(Alert.AlertType.WARNING, "Duplicate", "Condition already exists.");
                return;
            }
            if (!trimmed.isEmpty()) {
                // Async add to DB
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() {
                        service.addCondition(category, trimmed);
                        return null;
                    }
                };
                task.setOnFailed(e -> {
                    Throwable ex = task.getException();
                    showAlert(Alert.AlertType.ERROR, "Save Error", "Failed to add condition: " + ex.getMessage());
                });
                task.setOnSucceeded(e -> {
                    // Refresh list to keep ordering consistent with DB query
                    Task<Void> refreshTask = new Task<>() {
                        @Override
                        protected Void call() {
                            var updated = service.getConditions(category);
                            Platform.runLater(() -> {
                                FilteredList<String> fl = (FilteredList<String>) target.getItems();
                                @SuppressWarnings("unchecked")
                                ObservableList<String> source = (ObservableList<String>) fl.getSource();
                                source.setAll(updated);
                            });
                            return null;
                        }
                    };
                    refreshTask.setOnFailed(ev -> {
                        Throwable ex = refreshTask.getException();
                        showAlert(Alert.AlertType.ERROR, "Refresh Error", "Failed to refresh list: " + ex.getMessage());
                    });
                    new Thread(refreshTask).start();
                });
                new Thread(task).start();
            }
        });
    }

    @FXML
    private void handleEditCondition() {
        TargetSelection selection = getTargetSelection();
        if (selection == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Click on a condition list first (to give it focus).");
            return;
        }

        String selectedItem = selection.listView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "No Item Selected", "Please select an item to edit.");
            return;
        }
        
        handleEdit(selectedItem, selection.category, selection.listView);
    }

    @FXML
    private void handleDeleteCondition() {
        TargetSelection selection = getTargetSelection();
        if (selection == null) {
             showAlert(Alert.AlertType.WARNING, "No Selection", "Click on a condition list first (to give it focus).");
            return;
        }

        String selectedItem = selection.listView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "No Item Selected", "Please select an item to delete.");
            return;
        }

        handleDelete(selectedItem, selection.category, selection.listView);
    }

    private static class TargetSelection {
        final ListView<String> listView;
        final ConditionCategory category;

        TargetSelection(ListView<String> listView, ConditionCategory category) {
            this.listView = listView;
            this.category = category;
        }
    }

    private TargetSelection getTargetSelection() {
        if (endocrineList.isFocused()) return new TargetSelection(endocrineList, ConditionCategory.ENDOCRINE);
        if (cancerList.isFocused()) return new TargetSelection(cancerList, ConditionCategory.CANCER);
        if (cardioList.isFocused()) return new TargetSelection(cardioList, ConditionCategory.CARDIOVASCULAR);
        if (geneticList.isFocused()) return new TargetSelection(geneticList, ConditionCategory.GENETIC);
        if (lastFocusedSelection != null) return lastFocusedSelection;
        if (!endocrineList.getSelectionModel().getSelectedItems().isEmpty()) {
            return new TargetSelection(endocrineList, ConditionCategory.ENDOCRINE);
        }
        if (!cancerList.getSelectionModel().getSelectedItems().isEmpty()) {
            return new TargetSelection(cancerList, ConditionCategory.CANCER);
        }
        if (!cardioList.getSelectionModel().getSelectedItems().isEmpty()) {
            return new TargetSelection(cardioList, ConditionCategory.CARDIOVASCULAR);
        }
        if (!geneticList.getSelectionModel().getSelectedItems().isEmpty()) {
            return new TargetSelection(geneticList, ConditionCategory.GENETIC);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private boolean isDuplicateName(ListView<String> listView, String candidate, String ignoreName) {
        FilteredList<String> fl = (FilteredList<String>) listView.getItems();
        ObservableList<String> source = (ObservableList<String>) fl.getSource();
        String lowered = candidate.toLowerCase(Locale.ROOT);
        String ignoreLower = (ignoreName == null) ? null : ignoreName.toLowerCase(Locale.ROOT);
        for (String item : source) {
            if (ignoreLower != null && item.toLowerCase(Locale.ROOT).equals(ignoreLower)) {
                continue;
            }
            if (item.toLowerCase(Locale.ROOT).equals(lowered)) {
                return true;
            }
        }
        return false;
    }

    @FXML
    private void handleSaveLists() {
         showAlert(Alert.AlertType.INFORMATION, "Auto-Saved", "Conditions are now saved automatically to the database.");
    }

    @FXML
    private void handleClear() {
        historyTextArea.clear();
    }

    @FXML
    private void handleSaveToEMR() {
        if (textAreaManager == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "EMR Manager not connected.");
            return;
        }
        String text = historyTextArea.getText();
        if (text.trim().isEmpty()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "History is empty. Save anyway?", ButtonType.YES, ButtonType.NO);
            if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
                return;
            }
        }
        
        textAreaManager.insertBlockIntoArea(IAITextAreaManager.AREA_PMH, text, true);
        showAlert(Alert.AlertType.INFORMATION, "Success", "Family History saved to EMR.");
        handleClose();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) historyTextArea.getScene().getWindow();
        stage.close();
    }

    private void setupAbbreviation(TextArea ta) {
        ta.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE) {
                expandAbbreviation(ta);
            }
        });
    }

    private void expandAbbreviation(TextArea ta) {
        int caret = ta.getCaretPosition();
        String text = ta.getText();
        String upToCaret = text.substring(0, caret);
        int start = Math.max(upToCaret.lastIndexOf(' '), upToCaret.lastIndexOf('\n')) + 1;
        
        if (start >= caret) return; 
        
        String word = upToCaret.substring(start).trim();
        if (word.startsWith(":") && abbrevMap.containsKey(word.substring(1))) {
            String replacement = abbrevMap.get(word.substring(1));
            ta.deleteText(start, caret);
            ta.insertText(start, replacement + " ");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
