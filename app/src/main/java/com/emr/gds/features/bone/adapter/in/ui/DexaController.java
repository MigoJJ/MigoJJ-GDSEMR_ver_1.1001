package com.emr.gds.features.bone.adapter.in.ui;

import com.emr.gds.features.bone.application.*;
import com.emr.gds.features.bone.domain.*;
import com.emr.gds.infrastructure.service.EmrBridgeService;
import com.emr.gds.input.IAITextAreaManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.emr.gds.infrastructure.ai.AiAssistantService;
import com.emr.gds.context.AppContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DexaController {

    private static final Logger logger = LoggerFactory.getLogger(DexaController.class);

    private final DexaReportService reportService;
    private final DexaDiagnosisService diagnosisService;
    private final EmrBridgeService emrBridge = new EmrBridgeService();

    private DexaReport currentReport;
    private DexaDiagnosisResult currentDiagnosis;

    // UI Components
    private TextField nameField, idField, ageField, heightField, weightField;
    private DatePicker birthDatePicker, examDatePicker;
    private ComboBox<Sex> sexCombo;
    private TableView<MeasurementRow> spineTable, femurTable;
    private CheckBox fragilityCheckbox, hipVertebralCheckbox, postmenopausalCheckbox,
            hrtCheckbox, tahCheckbox, kidneyStonesCheckbox;
    private TextArea outputArea;
    private ScrollPane root;

    public DexaController(DexaReportService reportService, DexaDiagnosisService diagnosisService) {
        this.reportService = reportService;
        this.diagnosisService = diagnosisService;
        this.currentReport = new DexaReport();
        this.root = buildUI();
    }

    public ScrollPane getRoot() {
        return root;
    }

    private ScrollPane buildUI() {
        VBox mainPanel = new VBox(10);
        mainPanel.setPadding(new Insets(15));

        // Patient Info Section
        mainPanel.getChildren().add(createPatientInfoSection());

        // Measurements Section
        mainPanel.getChildren().add(createMeasurementsSection());

        // Risk Factors Section
        mainPanel.getChildren().add(createRiskFactorsSection());

        // Buttons Section
        mainPanel.getChildren().add(createButtonsSection());

        // Output Section
        mainPanel.getChildren().add(createOutputSection());

        ScrollPane scrollPane = new ScrollPane(mainPanel);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    private TitledPane createPatientInfoSection() {
        VBox vbox = new VBox(5);
        vbox.setPadding(new Insets(10));

        HBox row1 = new HBox(10);
        row1.getChildren().addAll(
                createLabeledField("Name:", nameField = new TextField()),
                createLabeledField("ID:", idField = new TextField()),
                createLabeledField("Age:", ageField = new TextField())
        );

        HBox row2 = new HBox(10);
        birthDatePicker = new DatePicker();
        sexCombo = new ComboBox<>();
        sexCombo.getItems().addAll(Sex.MALE, Sex.FEMALE, Sex.UNKNOWN);
        sexCombo.setValue(Sex.UNKNOWN);
        row2.getChildren().addAll(
                createLabeledField("Birth Date:", birthDatePicker),
                createLabeledField("Sex:", sexCombo)
        );

        HBox row3 = new HBox(10);
        row3.getChildren().addAll(
                createLabeledField("Height (cm):", heightField = new TextField()),
                createLabeledField("Weight (kg):", weightField = new TextField()),
                createLabeledField("Exam Date:", examDatePicker = new DatePicker())
        );

        vbox.getChildren().addAll(row1, row2, row3);
        return new TitledPane("Patient Information", vbox);
    }

    private VBox createMeasurementsSection() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        // Spine Measurements
        VBox spineSection = new VBox(5);
        spineSection.getChildren().add(new Label("Spine (Lumbar) Measurements:"));
        spineTable = createMeasurementTable();
        spineSection.getChildren().add(spineTable);
        HBox spineButtons = new HBox(5);
        spineButtons.getChildren().addAll(
                createButton("Add Row", e -> addSpineRow()),
                createButton("Remove Row", e -> removeSpineRow())
        );
        spineSection.getChildren().add(spineButtons);

        // Femur Measurements
        VBox femurSection = new VBox(5);
        femurSection.getChildren().add(new Label("Femur (Hip) Measurements:"));
        femurTable = createMeasurementTable();
        femurSection.getChildren().add(femurTable);
        HBox femurButtons = new HBox(5);
        femurButtons.getChildren().addAll(
                createButton("Add Row", e -> addFemurRow()),
                createButton("Remove Row", e -> removeFemurRow())
        );
        femurSection.getChildren().add(femurButtons);

        vbox.getChildren().addAll(spineSection, femurSection);
        return vbox;
    }

    private VBox createRiskFactorsSection() {
        VBox vbox = new VBox(5);
        vbox.setPadding(new Insets(10));

        Label label = new Label("Risk Factors:");
        fragilityCheckbox = new CheckBox("Fragility Fracture");
        hipVertebralCheckbox = new CheckBox("Hip or Vertebral Fracture");
        postmenopausalCheckbox = new CheckBox("Postmenopausal");
        hrtCheckbox = new CheckBox("On HRT");
        tahCheckbox = new CheckBox("Prior TAH");
        kidneyStonesCheckbox = new CheckBox("Kidney Stones");

        FlowPane flowPane = new FlowPane(10, 10);
        flowPane.getChildren().addAll(
                fragilityCheckbox, hipVertebralCheckbox, postmenopausalCheckbox,
                hrtCheckbox, tahCheckbox, kidneyStonesCheckbox
        );

        vbox.getChildren().addAll(label, flowPane);
        return vbox;
    }

    private HBox createButtonsSection() {
        HBox hbox = new HBox(10);
        hbox.setPadding(new Insets(10));

        hbox.getChildren().addAll(
                createButton("Load DEXA Image", e -> loadDexaImage()),
                createButton("Assess", e -> assess()),
                createButton("Save to EMR & DB", e -> save()),
                createButton("Clear", e -> clear())
        );

        return hbox;
    }

    private VBox createOutputSection() {
        VBox vbox = new VBox(5);
        vbox.setPadding(new Insets(10));

        Label label = new Label("Diagnosis & Output:");
        outputArea = new TextArea();
        outputArea.setWrapText(true);
        outputArea.setPrefRowCount(8);
        outputArea.setEditable(false);

        vbox.getChildren().addAll(label, outputArea);
        VBox.setVgrow(outputArea, Priority.ALWAYS);

        return vbox;
    }

    private TableView<MeasurementRow> createMeasurementTable() {
        TableView<MeasurementRow> table = new TableView<>();
        table.setPrefHeight(150);
        table.setEditable(true);

        TableColumn<MeasurementRow, String> regionCol = new TableColumn<>("Region");
        regionCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().region));
        regionCol.setCellFactory(TextFieldTableCell.forTableColumn());
        regionCol.setOnEditCommit(e -> e.getRowValue().region = e.getNewValue());

        TableColumn<MeasurementRow, Double> bmdCol = new TableColumn<>("BMD");
        bmdCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().bmd));
        bmdCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        bmdCol.setOnEditCommit(e -> e.getRowValue().bmd = e.getNewValue());

        TableColumn<MeasurementRow, Double> tScoreCol = new TableColumn<>("T-Score");
        tScoreCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().tScore));
        tScoreCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        tScoreCol.setOnEditCommit(e -> e.getRowValue().tScore = e.getNewValue());

        TableColumn<MeasurementRow, Double> zScoreCol = new TableColumn<>("Z-Score");
        zScoreCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().zScore));
        zScoreCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        zScoreCol.setOnEditCommit(e -> e.getRowValue().zScore = e.getNewValue());

        table.getColumns().addAll(regionCol, bmdCol, tScoreCol, zScoreCol);
        return table;
    }

    private void loadDexaImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select DEXA Report Image");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp", "*.webp"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Stage stage = (Stage) nameField.getScene().getWindow();
        java.io.File file = chooser.showOpenDialog(stage);

        if (file == null) {
            return;
        }

        Path imagePath = file.toPath();
        String prompt = DexaVisionPromptBuilder.buildExtractionPrompt();

        outputArea.appendText("AI 이미지 판독 중...\n");

        try {
            AiAssistantService aiService = AppContext.getInstance().getAiAssistantService();
            aiService.generateClinicalDraft(prompt, List.of(imagePath))
                    .thenApply(DexaVisionResponseParser::parse)
                    .thenAccept(report -> Platform.runLater(() -> {
                        currentReport = report;
                        populateUIFromReport(report);
                        // Archive image
                        try {
                            Path imageDir = com.emr.gds.core.config.RuntimeEnvironment.getImageDirectory();
                            Files.createDirectories(imageDir);
                            Path dexaDir = imageDir.resolve("dexa");
                            Files.createDirectories(dexaDir);
                            Path archivedPath = dexaDir.resolve(UUID.randomUUID() + ".jpg");
                            Files.copy(imagePath, archivedPath);
                            currentReport.setSourceImagePath(archivedPath.toString());
                        } catch (Exception e) {
                            logger.warn("이미지 복사 실패", e);
                        }
                        outputArea.appendText("이미지 판독 완료!\n");
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            outputArea.appendText("AI 판독 실패: " + ex.getMessage() + "\n");
                            logger.warn("AI 판독 실패", ex);
                            showAlert(Alert.AlertType.WARNING,
                                    "AI 판독 실패",
                                    "이미지 판독에 실패했습니다. 수동으로 입력해주세요.");
                        });
                        return null;
                    });
        } catch (Exception e) {
            logger.error("AI 서비스 호출 실패", e);
            showAlert(Alert.AlertType.ERROR, "오류", "AI 서비스를 호출할 수 없습니다.");
        }
    }

    private void populateUIFromReport(DexaReport report) {
        nameField.setText(report.getPatientName() != null ? report.getPatientName() : "");
        idField.setText(report.getPatientId() != null ? report.getPatientId() : "");
        if (report.getAge() != null) ageField.setText(report.getAge().toString());
        if (report.getBirthDate() != null) birthDatePicker.setValue(report.getBirthDate());
        if (report.getSex() != null) sexCombo.setValue(report.getSex());
        if (report.getHeightCm() != null) heightField.setText(report.getHeightCm().toString());
        if (report.getWeightKg() != null) weightField.setText(report.getWeightKg().toString());
        if (report.getExamDate() != null) examDatePicker.setValue(report.getExamDate());

        // Populate tables
        spineTable.getItems().clear();
        for (DexaMeasurement m : report.getSpineMeasurements()) {
            spineTable.getItems().add(new MeasurementRow(m.region(), m.bmd(), m.tScore(), m.zScore()));
        }

        femurTable.getItems().clear();
        for (DexaMeasurement m : report.getFemurMeasurements()) {
            femurTable.getItems().add(new MeasurementRow(m.region(), m.bmd(), m.tScore(), m.zScore()));
        }
    }

    private void assess() {
        try {
            updateReportFromUI();
            DexaRiskFactors factors = collectRiskFactors();
            currentDiagnosis = diagnosisService.diagnose(currentReport, factors);

            outputArea.clear();
            String reportText = reportService.buildReportText(currentReport, currentDiagnosis);
            outputArea.setText(reportText);
        } catch (IllegalStateException e) {
            showAlert(Alert.AlertType.WARNING, "진단 불가", e.getMessage());
            logger.warn("진단 실패", e);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "오류", "진단 중 오류가 발생했습니다.");
            logger.error("진단 중 오류", e);
        }
    }

    private void save() {
        try {
            if (currentDiagnosis == null) {
                showAlert(Alert.AlertType.WARNING, "경고", "먼저 Assess를 실행하세요.");
                return;
            }

            updateReportFromUI();
            currentReport.setDiagnosis(currentDiagnosis.diagnosis());
            currentReport.setDiagnosisRationale(currentDiagnosis.rationale());
            currentReport.setCreatedAt(LocalDate.now());

            reportService.saveReport(currentReport);

            String reportText = reportService.buildReportText(currentReport, currentDiagnosis);
            boolean pushed = emrBridge.insertLine(IAITextAreaManager.AREA_A, reportText);

            if (pushed) {
                showAlert(Alert.AlertType.INFORMATION, "저장 완료", "DEXA 보고서가 저장되고 EMR에 삽입되었습니다.");
            } else {
                showAlert(Alert.AlertType.WARNING, "부분 성공", "DB에는 저장되었지만 EMR 삽입에 실패했습니다.");
            }
        } catch (DexaPersistenceException e) {
            showAlert(Alert.AlertType.ERROR, "저장 실패", "DB 저장에 실패했습니다: " + e.getMessage());
            logger.error("DB 저장 실패", e);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "오류", "저장 중 오류가 발생했습니다.");
            logger.error("저장 중 오류", e);
        }
    }

    private void clear() {
        nameField.clear();
        idField.clear();
        ageField.clear();
        heightField.clear();
        weightField.clear();
        birthDatePicker.setValue(null);
        examDatePicker.setValue(null);
        sexCombo.setValue(Sex.UNKNOWN);
        spineTable.getItems().clear();
        femurTable.getItems().clear();
        fragilityCheckbox.setSelected(false);
        hipVertebralCheckbox.setSelected(false);
        postmenopausalCheckbox.setSelected(false);
        hrtCheckbox.setSelected(false);
        tahCheckbox.setSelected(false);
        kidneyStonesCheckbox.setSelected(false);
        outputArea.clear();
        currentReport = new DexaReport();
        currentDiagnosis = null;
    }

    private void updateReportFromUI() {
        currentReport.setPatientName(nameField.getText().isBlank() ? null : nameField.getText());
        currentReport.setPatientId(idField.getText().isBlank() ? null : idField.getText());
        currentReport.setAge(ageField.getText().isBlank() ? null : tryParseInt(ageField.getText()));
        currentReport.setBirthDate(birthDatePicker.getValue());
        currentReport.setSex(sexCombo.getValue());
        currentReport.setHeightCm(heightField.getText().isBlank() ? null : tryParseDouble(heightField.getText()));
        currentReport.setWeightKg(weightField.getText().isBlank() ? null : tryParseDouble(weightField.getText()));
        currentReport.setExamDate(examDatePicker.getValue());

        currentReport.getSpineMeasurements().clear();
        for (MeasurementRow row : spineTable.getItems()) {
            currentReport.getSpineMeasurements().add(
                    new DexaMeasurement(row.region, SkeletalSite.SPINE, row.bmd, row.tScore, row.zScore));
        }

        currentReport.getFemurMeasurements().clear();
        for (MeasurementRow row : femurTable.getItems()) {
            currentReport.getFemurMeasurements().add(
                    new DexaMeasurement(row.region, SkeletalSite.FEMUR, row.bmd, row.tScore, row.zScore));
        }
    }

    private DexaRiskFactors collectRiskFactors() {
        return new DexaRiskFactors(
                fragilityCheckbox.isSelected(),
                hipVertebralCheckbox.isSelected(),
                postmenopausalCheckbox.isSelected(),
                hrtCheckbox.isSelected(),
                tahCheckbox.isSelected(),
                kidneyStonesCheckbox.isSelected()
        );
    }

    private void addSpineRow() {
        spineTable.getItems().add(new MeasurementRow("", null, null, null));
    }

    private void removeSpineRow() {
        int idx = spineTable.getSelectionModel().getSelectedIndex();
        if (idx >= 0) {
            spineTable.getItems().remove(idx);
        }
    }

    private void addFemurRow() {
        femurTable.getItems().add(new MeasurementRow("", null, null, null));
    }

    private void removeFemurRow() {
        int idx = femurTable.getSelectionModel().getSelectedIndex();
        if (idx >= 0) {
            femurTable.getItems().remove(idx);
        }
    }

    private HBox createLabeledField(String label, Control control) {
        HBox hbox = new HBox(5);
        hbox.getChildren().add(new Label(label));
        hbox.getChildren().add(control);
        HBox.setHgrow(control, Priority.ALWAYS);
        return hbox;
    }

    private Button createButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(text);
        btn.setOnAction(handler);
        return btn;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private Integer tryParseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double tryParseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Simple data holder for table rows
    public static class MeasurementRow {
        public String region;
        public Double bmd;
        public Double tScore;
        public Double zScore;

        public MeasurementRow(String region, Double bmd, Double tScore, Double zScore) {
            this.region = region;
            this.bmd = bmd;
            this.tScore = tScore;
            this.zScore = zScore;
        }
    }
}
