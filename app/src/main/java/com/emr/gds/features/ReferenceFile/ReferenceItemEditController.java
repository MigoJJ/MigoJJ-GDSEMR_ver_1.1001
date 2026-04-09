package com.emr.gds.features.ReferenceFile;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ReferenceItemEditController {

    @FXML
    private Label titleLabel;
    @FXML
    private TextField categoryField;
    @FXML
    private TextField contentsField;
    @FXML
    private TextField directoryPathField;

    private Stage dialogStage;
    private ReferenceItem referenceItem;
    private boolean saveClicked = false;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setReferenceItem(ReferenceItem referenceItem) {
        this.referenceItem = referenceItem;

        if (referenceItem != null) {
            // Pre-fill fields if editing an existing item
            titleLabel.setText("Edit Reference Item");
            categoryField.setText(referenceItem.getCategory());
            contentsField.setText(referenceItem.getContents());
            directoryPathField.setText(referenceItem.getDirectoryPath());
        } else {
            // For new items
            titleLabel.setText("Add New Reference Item");
        }
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    public ReferenceItem getReferenceItem() {
        // Return a new ReferenceItem with updated values, or the modified existing one
        if (referenceItem == null) {
            referenceItem = new ReferenceItem(); // For new items
        }
        referenceItem.setCategory(categoryField.getText());
        referenceItem.setContents(contentsField.getText());
        referenceItem.setDirectoryPath(directoryPathField.getText());
        return referenceItem;
    }

    @FXML
    private void handleSave() {
        // Basic validation
        if (isInputValid()) {
            saveClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (categoryField.getText() == null || categoryField.getText().isEmpty()) {
            errorMessage += "No valid category!\n";
        }
        if (contentsField.getText() == null || contentsField.getText().isEmpty()) {
            errorMessage += "No valid contents!\n";
        }
        if (directoryPathField.getText() == null || directoryPathField.getText().isEmpty()) {
            errorMessage += "No valid directory path!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            // Show the error message.
            // This is a simple alert; a more sophisticated UI might show inline errors.
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);

            alert.showAndWait();
            return false;
        }
    }
}
