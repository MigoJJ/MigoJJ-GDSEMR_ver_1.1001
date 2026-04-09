package com.emr.gds.features.review_of_systems;

import com.emr.gds.soap.ros.EMR_ROS_JtableDATA;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class ReviewOfSystemsEditor {

    private final TextArea targetTextArea;
    private final Map<String, List<CheckBox>> categoryCheckBoxes = new HashMap<>();

    public ReviewOfSystemsEditor(TextArea targetTextArea) {
        this.targetTextArea = targetTextArea;
    }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Review of Systems (ROS) Editor");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        TabPane tabPane = createTabPane();
        root.setCenter(tabPane);

        TextArea reportPreview = new TextArea();
        reportPreview.setPromptText("Generated report will appear here...");
        reportPreview.setWrapText(true);
        reportPreview.setPrefHeight(150);

        Button generateButton = new Button("Generate Report");
        generateButton.setOnAction(e -> reportPreview.setText(generateReport()));

        Button addToChartButton = new Button("Add to Chart");
        addToChartButton.setOnAction(e -> {
            String report = reportPreview.getText();
            if (report != null && !report.isEmpty()) {
                targetTextArea.appendText(report);
            }
            stage.close();
        });

        HBox buttonBox = new HBox(10, generateButton, addToChartButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        VBox bottomPane = new VBox(10, new Separator(), reportPreview, buttonBox);
        root.setBottom(bottomPane);

        Scene scene = new Scene(root, 800, 700);
        stage.setScene(scene);
        stage.show();
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        String[] categories = EMR_ROS_JtableDATA.columnNames();

        for (String category : categories) {
            Tab tab = new Tab(category.replace("<", "").replace(">", "").trim());
            VBox content = new VBox(5);
            content.setPadding(new Insets(10));

            List<CheckBox> checkBoxes = new ArrayList<>();
            categoryCheckBoxes.put(category, checkBoxes);

            String[] items = getItemsForCategory(category);
            for (String item : items) {
                CheckBox cb = new CheckBox(item);
                checkBoxes.add(cb);
                content.getChildren().add(cb);
            }
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            tab.setContent(scrollPane);
            tabPane.getTabs().add(tab);
        }
        return tabPane;
    }

    private String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("REVIEW OF SYSTEMS:\n");
        report.append("=========================\n\n");

        boolean hasSelections = false;

        for (String category : EMR_ROS_JtableDATA.columnNames()) {
            List<CheckBox> checkBoxes = categoryCheckBoxes.get(category);
            List<String> selectedItems = new ArrayList<>();
            List<String> deniedItems = new ArrayList<>();

            for (CheckBox cb : checkBoxes) {
                if (cb.isSelected()) {
                    selectedItems.add(cb.getText());
                } else {
                    deniedItems.add(cb.getText());
                }
            }

            if (!selectedItems.isEmpty()) { // Only include category if there's at least one selected item
                hasSelections = true;
                String categoryName = category.replace("<", "").replace(">", "").trim();
                report.append(String.format("%-45s\n", categoryName.toUpperCase() + ":"));

                int maxRows = Math.max(selectedItems.size(), deniedItems.size());
                for (int i = 0; i < maxRows; i++) {
                    String leftCol = "";
                    if (i < selectedItems.size()) {
                        leftCol = "    [+] " + selectedItems.get(i);
                    }

                    String rightCol = "";
                    if (i < deniedItems.size()) {
                        rightCol = "[-] " + deniedItems.get(i);
                    }
                    report.append(String.format("    %-40s %s\n", leftCol, rightCol));
                }
                report.append("\n");
            }
        }

        if (!hasSelections) {
            return "No symptoms selected.";
        }

        report.append("=========================\n\n");
        return report.toString();
    }

    private String[] getItemsForCategory(String category) {
        switch (category) {
            case "< General >":
                return EMR_ROS_JtableDATA.General();
            case "< Vision >":
                return EMR_ROS_JtableDATA.Vision();
            case "< Head_and_Neck >":
                return EMR_ROS_JtableDATA.Head_and_Neck();
            case "< Pulmonary >":
                return EMR_ROS_JtableDATA.Pulmonary();
            case "< Cardiovascular >":
                return EMR_ROS_JtableDATA.Cardiovascular();
            case "< Gastrointestinal >":
                return EMR_ROS_JtableDATA.Gastrointestinal();
            case "< Genito-Urinary >":
                return EMR_ROS_JtableDATA.GenitoUrinary();
            case "< Hematology/Oncology >":
                return EMR_ROS_JtableDATA.HematologyOncology();
            case "< Neurological >":
                return EMR_ROS_JtableDATA.Neurological();
            case "< Endocrine >":
                return EMR_ROS_JtableDATA.Endocrine();
            case "< Mental Health >":
                return EMR_ROS_JtableDATA.MentalHealth();
            case "< Skin and Hair >":
                return EMR_ROS_JtableDATA.SkinAndHair();
            default:
                return new String[0];
        }
    }
}
