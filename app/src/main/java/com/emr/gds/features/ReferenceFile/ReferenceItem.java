package com.emr.gds.features.ReferenceFile;

import javafx.beans.property.SimpleStringProperty;

public class ReferenceItem {
    private int id; // For database mapping
    private final SimpleStringProperty category;
    private final SimpleStringProperty contents;
    private final SimpleStringProperty directoryPath;

    // Constructor for Jackson deserialization and new items (ID will be set by DB)
    public ReferenceItem() {
        this(0, "", "", "");
    }

    // Constructor for creating new items without an ID (ID will be generated)
    public ReferenceItem(String category, String contents, String directoryPath) {
        this(0, category, contents, directoryPath);
    }

    // Full constructor for loading from DB
    public ReferenceItem(int id, String category, String contents, String directoryPath) {
        this.id = id;
        this.category = new SimpleStringProperty(category);
        this.contents = new SimpleStringProperty(contents);
        this.directoryPath = new SimpleStringProperty(directoryPath);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category.get();
    }

    public SimpleStringProperty categoryProperty() {
        return category;
    }

    public void setCategory(String category) {
        this.category.set(category);
    }

    public String getContents() {
        return contents.get();
    }

    public SimpleStringProperty contentsProperty() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents.set(contents);
    }

    public String getDirectoryPath() {
        return directoryPath.get();
    }

    public SimpleStringProperty directoryPathProperty() {
        return directoryPath;
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath.set(directoryPath);
    }
}
