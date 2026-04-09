package com.emr.gds.soap.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;

public class PMHEntryViewModel {
    private final PMHEntry pmhEntry; // Reference to the underlying model

    private final StringProperty category = new SimpleStringProperty();
    private final BooleanProperty selected = new SimpleBooleanProperty();
    private final StringProperty notes = new SimpleStringProperty();

    public PMHEntryViewModel(PMHEntry pmhEntry) {
        this.pmhEntry = pmhEntry;
        // Initialize properties from the model
        this.category.set(pmhEntry.getCategory());
        this.selected.set(pmhEntry.isSelected());
        this.notes.set(pmhEntry.getNotes());

        // Listen for changes in ViewModel properties and update the model
        this.selected.addListener((obs, oldVal, newVal) -> pmhEntry.setSelected(newVal));
        this.notes.addListener((obs, oldVal, newVal) -> pmhEntry.setNotes(newVal));
        // Category is usually static, so no listener needed for it to update the model.
    }

    // Bidirectional binding to UI components
    public void bindToUI(CheckBox cb, TextArea ta) {
        cb.selectedProperty().bindBidirectional(selected);
        ta.textProperty().bindBidirectional(notes);
    }

    // Getters for properties
    public StringProperty categoryProperty() {
        return category;
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public StringProperty notesProperty() {
        return notes;
    }

    // Getters for current values (convenience)
    public String getCategory() {
        return category.get();
    }

    public boolean isSelected() {
        return selected.get();
    }

    public String getNotes() {
        return notes.get();
    }

    // You might want to expose the underlying model or parts of it
    public PMHEntry getPmhEntry() {
        return pmhEntry;
    }
}
