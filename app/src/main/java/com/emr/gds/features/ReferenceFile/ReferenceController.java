package com.emr.gds.features.ReferenceFile;

import com.emr.gds.features.ReferenceFile.ReferenceItem;
import com.emr.gds.service.ReferenceService; // New import
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.layout.VBox; // New import
import java.io.IOException; // Re-added import
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ReferenceController implements Initializable {

    @FXML
    private TextField searchField;
    @FXML
    private TableView<ReferenceItem> referenceTable;
    @FXML
    private TableColumn<ReferenceItem, String> colCategory;
    @FXML
    private TableColumn<ReferenceItem, String> colContents;
    @FXML
    private TableColumn<ReferenceItem, String> colDirectoryPath; // New column

    private ObservableList<ReferenceItem> masterData = FXCollections.observableArrayList();
    private ObservableList<ReferenceItem> filteredData = FXCollections.observableArrayList();

    private File basePath; // Injected base path
    private ReferenceService referenceService; // Injected service

    public void setBasePath(File basePath) {
        this.basePath = basePath;
    }

    public void setReferenceService(ReferenceService referenceService) {
        this.referenceService = referenceService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colContents.setCellValueFactory(new PropertyValueFactory<>("contents"));
        colDirectoryPath.setCellValueFactory(new PropertyValueFactory<>("directoryPath")); // Set cell value factory for new column

        // Data loading and initial table population will be handled by initData()
        // which is called after basePath is set.

        // Add click listener to table rows
        referenceTable.setOnMouseClicked(this::handleTableClick);
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase();
        filteredData.clear();
        if (query.isEmpty()) {
            filteredData.addAll(masterData);
        } else {
            for (ReferenceItem item : masterData) {
                if (item.getCategory().toLowerCase().contains(query) ||
                    item.getContents().toLowerCase().contains(query) ||
                    item.getDirectoryPath().toLowerCase().contains(query)) { // Search in directory path too
                    filteredData.add(item);
                }
            }
        }
    }

    private void handleTableClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            ReferenceItem selectedItem = referenceTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                File targetDirectory = resolveReferenceDirectory(selectedItem.getDirectoryPath());
                if (targetDirectory != null) {
                    openDirectoryInFileExplorer(targetDirectory);
                } else {
                    showAlert("Invalid Path", "No valid directory configured for this reference.");
                }
            }
        }
    }

    private File resolveReferenceDirectory(String directoryPath) {
        if (this.basePath == null) {
            // Fallback or error handling if basePath is not set.
            // For now, let's just return null if basePath is not properly initialized.
            System.err.println("Error: ReferenceController.basePath is not set.");
            return null;
        }

        File currentBaseDir = this.basePath;
        if (!currentBaseDir.exists() && !currentBaseDir.mkdirs()) {
            return null;
        }
        if (directoryPath == null || directoryPath.trim().isEmpty()) {
            return currentBaseDir.exists() ? currentBaseDir : null;
        }

        String normalized = directoryPath.trim();
        File candidate = new File(normalized);
        if (candidate.isAbsolute() && candidate.exists() && candidate.isDirectory()) {
            return candidate;
        }

        while (normalized.startsWith(File.separator) || normalized.startsWith("/") || normalized.startsWith("\\")) {
            normalized = normalized.substring(1);
        }

        if (!normalized.isEmpty()) {
            candidate = new File(normalized);
        }
        if (!candidate.isAbsolute()) {
            candidate = new File(currentBaseDir, directoryPath);
        }

        if (candidate.exists() && candidate.isDirectory()) {
            return candidate;
        }

        return null;
    }


    private void openDirectoryInFileExplorer(File directory) {
        try {
            if (directory.exists() && directory.isDirectory()) {
                // For Windows
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    Runtime.getRuntime().exec("explorer.exe " + directory.getAbsolutePath());
                }
                // For Mac
                else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                    Runtime.getRuntime().exec("open " + directory.getAbsolutePath());
                }
                // For Linux
                else if (System.getProperty("os.name").toLowerCase().contains("nix") || System.getProperty("os.name").toLowerCase().contains("nux")) {
                    Runtime.getRuntime().exec("xdg-open " + directory.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            System.err.println("Error opening directory in file explorer: " + e.getMessage());
            showAlert("Error", "Could not open directory in file explorer.");
        }
    }

    @FXML
    private void handleAdd() {
        try {
            // Load the FXML file for the dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reference_item_edit.fxml"));
            VBox page = loader.load();

            // Create the dialog Stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add Reference Item");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(referenceTable.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the item into the controller
            ReferenceItemEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setReferenceItem(null); // Indicates add mode

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                ReferenceItem newItem = controller.getReferenceItem();
                if (newItem != null) {
                    referenceService.saveReference(newItem); // Save via service
                    masterData.add(newItem); // Add the saved item (with ID) to masterData
                    handleSearch(); // Refresh the table
                }
            }
        } catch (IOException e) {
            System.err.println("Error opening reference item edit dialog: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Could not open dialog to add reference item.");
        }
    }

    @FXML
    private void handleEdit() {
        ReferenceItem selectedItem = referenceTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            try {
                // Load the FXML file for the dialog
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reference_item_edit.fxml"));
                VBox page = loader.load();

                // Create the dialog Stage
                Stage dialogStage = new Stage();
                dialogStage.setTitle("Edit Reference Item");
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.initOwner(referenceTable.getScene().getWindow());
                Scene scene = new Scene(page);
                dialogStage.setScene(scene);

                // Set the item into the controller
                ReferenceItemEditController controller = loader.getController();
                controller.setDialogStage(dialogStage);
                controller.setReferenceItem(selectedItem); // Pass the selected item for editing

                // Show the dialog and wait until the user closes it
                dialogStage.showAndWait();

                if (controller.isSaveClicked()) {
                    ReferenceItem editedItem = controller.getReferenceItem();
                    if (editedItem != null) {
                        // The editedItem is the same object as selectedItem,
                        // so its properties are already updated. Just save.
                        referenceService.saveReference(editedItem); // Save the updated item
                        referenceTable.refresh(); // Refresh the table display
                    }
                }
            } catch (IOException e) {
                System.err.println("Error opening reference item edit dialog: " + e.getMessage());
                e.printStackTrace();
                showAlert("Error", "Could not open dialog to edit reference item.");
            }
        } else {
            showAlert("No Selection", "Please select a reference to edit.");
        }
    }

    @FXML
    private void handleDelete() {
        ReferenceItem selectedItem = referenceTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Deletion");
            alert.setHeaderText("Delete Reference: " + selectedItem.getContents());
            alert.setContentText("Are you sure you want to delete this reference?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                referenceService.deleteReference(selectedItem); // Delete via service
                masterData.remove(selectedItem);
                handleSearch(); // Refresh the table
            }
        } else {
            showAlert("No Selection", "Please select a reference to delete.");
        }
    }

    @FXML
    private void handleFind() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Find Reference");
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            System.out.println("Selected file for Find: " + file.getAbsolutePath());
            showAlert("Find Action", "Searching for content related to: " + file.getName());
            // In a real application, you would implement search logic based on the file
        }
    }

    @FXML
    private void handleSave() {
        showAlert("Save Information", "Changes are automatically saved to the database.");
    }

    @FXML
    private void handleQuit() {
        Stage stage = (Stage) referenceTable.getScene().getWindow();
        stage.close();
    }

    public void initData() {
        if (referenceService == null) {
            System.err.println("Error: ReferenceService is not set.");
            return;
        }
        masterData.addAll(referenceService.findAllReferences());

        if (masterData.isEmpty()) { // Add sample data if DB is empty
            masterData.add(new ReferenceItem("Drug Information", "Medication A - side effects, dosage", "drugs/med_a"));
            masterData.add(new ReferenceItem("Guidelines", "Hypertension management guidelines 2023", "guidelines/hypertension"));
            masterData.add(new ReferenceItem("Lab Values", "Normal range for Hemoglobin A1c", "labs/hba1c"));
            masterData.add(new ReferenceItem("Drug Information", "Medication B - interactions", "drugs/med_b"));
        }
        
        filteredData.addAll(masterData);
        referenceTable.setItems(filteredData);
    }



    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
