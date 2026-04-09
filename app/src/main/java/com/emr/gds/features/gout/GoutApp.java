package com.emr.gds.features.gout;

import com.emr.gds.infrastructure.service.EmrBridgeService;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class GoutApp extends Application {

    private final EmrBridgeService bridge = new EmrBridgeService();

    @Override
    public void start(Stage stage) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-font-family: 'Segoe UI', sans-serif;");

        Label title = new Label("통풍(Gout) 진단 점수 계산기 (2015 ACR/EULAR)");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // 0. 확정적 진단 (Sufficient Criterion)
        CheckBox cbMsu = new CheckBox("증상 관절/점액낭에서 요산 결정(MSU) 확인됨");
        cbMsu.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        // 1. 임상적 기준
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        // 관절 침범
        grid.add(new Label("관절 침범:"), 0, 0);
        ComboBox<String> comboJoint = new ComboBox<>();
        comboJoint.getItems().addAll("해당 없음 (0점)", "발목 또는 발등 (1점)", "엄지발가락 MTP1 (2점)");
        comboJoint.getSelectionModel().selectFirst();
        grid.add(comboJoint, 1, 0);

        // 증상 특징
        grid.add(new Label("임상 특징 (발적, 압통, 보행장애):"), 0, 1);
        ComboBox<Integer> comboFeatures = new ComboBox<>();
        comboFeatures.getItems().addAll(0, 1, 2, 3);
        comboFeatures.getSelectionModel().selectFirst();
        grid.add(comboFeatures, 1, 1);

        // 시간적 양상
        grid.add(new Label("발작 양상:"), 0, 2);
        ComboBox<String> comboTime = new ComboBox<>();
        comboTime.getItems().addAll("없음 (0점)", "1회 전형적 발작 (1점)", "재발성 전형적 발작 (2점)");
        comboTime.getSelectionModel().selectFirst();
        grid.add(comboTime, 1, 2);

        // 통풍 결절
        CheckBox cbTophus = new CheckBox("통풍 결절(Tophus) 존재 (+4점)");
        grid.add(cbTophus, 1, 3);

        // 2. 검사실 기준
        grid.add(new Label("혈청 요산 농도 (mg/dL):"), 0, 4);
        ComboBox<String> comboUrate = new ComboBox<>();
        comboUrate.getItems().addAll("< 4 (-4점)", "4 ~ < 6 (0점)", "6 ~ < 8 (2점)", "8 ~ < 10 (3점)", "≥ 10 (4점)");
        comboUrate.getSelectionModel().select(1);
        grid.add(comboUrate, 1, 4);

        CheckBox cbFluidNeg = new CheckBox("관절액 검사 요산 결정 미검출 (-2점)");
        grid.add(cbFluidNeg, 1, 5);

        // 3. 영상 기준
        CheckBox cbImaging = new CheckBox("초음파 이중윤곽 또는 DECT 요산 확인 (+4점)");
        CheckBox cbErosion = new CheckBox("X-ray 골미란 확인 (+4점)");
        grid.add(cbImaging, 1, 6);
        grid.add(cbErosion, 1, 7);

        // 결과 영역
        Button btnCalc = new Button("점수 계산");
        btnCalc.setStyle("-fx-base: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
        Label lblResult = new Label("결과: -");
        lblResult.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextArea txtSummary = new TextArea();
        txtSummary.setEditable(false);
        txtSummary.setPrefHeight(200);
        txtSummary.setPromptText("계산 버튼을 누르면 상세 결과가 여기에 표시됩니다.");

        btnCalc.setOnAction(e -> {
            StringBuilder summary = new StringBuilder();
            summary.append("--- 통풍(Gout) 진단 평가 상세 내역 ---\n");

            if (cbMsu.isSelected()) {
                summary.append("[확정적 기준] 증상 관절/점액낭에서 요산 결정(MSU) 확인됨\n");
                summary.append("\n최종 판정: 확정적 통풍 (Sufficient Criterion 충족)");
                
                lblResult.setText("결과: 확정적 통풍 (Sufficient Criterion 충족)");
                lblResult.setStyle("-fx-text-fill: red; -fx-font-size: 16px; -fx-font-weight: bold;");
                txtSummary.setText(summary.toString());
                return;
            }

            int score = 0;
            summary.append(String.format("- 관절 침범: %s\n", comboJoint.getValue()));
            score += comboJoint.getSelectionModel().getSelectedIndex();

            summary.append(String.format("- 임상 특징(발적, 압통, 보행장애): %d개 특징 관찰\n", comboFeatures.getValue()));
            score += comboFeatures.getValue();

            summary.append(String.format("- 발작 양상: %s\n", comboTime.getValue()));
            score += comboTime.getSelectionModel().getSelectedIndex();

            if (cbTophus.isSelected()) {
                summary.append("- 통풍 결절(Tophus): 존재 (+4점)\n");
                score += 4;
            }

            summary.append(String.format("- 혈청 요산 농도: %s\n", comboUrate.getValue()));
            int urateIdx = comboUrate.getSelectionModel().getSelectedIndex();
            if (urateIdx == 0) score -= 4;
            else if (urateIdx == 2) score += 2;
            else if (urateIdx == 3) score += 3;
            else if (urateIdx == 4) score += 4;

            if (cbFluidNeg.isSelected()) {
                summary.append("- 관절액 검사: MSU 미검출 (-2점)\n");
                score -= 2;
            }
            if (cbImaging.isSelected()) {
                summary.append("- 영상(US/DECT): 요산 침착 확인 (+4점)\n");
                score += 4;
            }
            if (cbErosion.isSelected()) {
                summary.append("- 영상(X-ray): 골미란 확인 (+4점)\n");
                score += 4;
            }

            String diagnosis = (score >= 8) ? "[통풍 분류 가능 (Gout Classified)]" : "[통풍 아님 (Not Classified)]";
            summary.append("\n-----------------------------------\n");
            summary.append(String.format("총점: %d점\n최종 판정: %s", score, diagnosis));

            lblResult.setText(String.format("총점: %d점 - %s", score, diagnosis));
            lblResult.setStyle(score >= 8 ? "-fx-text-fill: red;" : "-fx-text-fill: black;");
            txtSummary.setText(summary.toString());
        });

        Button btnCopy = new Button("결과 복사");
        btnCopy.setOnAction(e -> {
            String text = txtSummary.getText();
            if (text != null && !text.isEmpty()) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(text);
                clipboard.setContent(content);
            }
        });

        Button btnSaveEmr = new Button("EMR에 저장");
        btnSaveEmr.setStyle("-fx-base: #3498db; -fx-text-fill: white;");
        btnSaveEmr.setOnAction(e -> {
            String text = txtSummary.getText();
            if (text == null || text.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "저장할 결과가 없습니다. 먼저 계산을 완료해 주세요.");
                alert.showAndWait();
                return;
            }
            // EMR Target Area Index 5 (Objective/Assessment area usually)
            boolean success = bridge.insertBlock(5, "\n" + text + "\n");
            if (success) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "EMR에 성공적으로 저장되었습니다.");
                alert.showAndWait();
                stage.close();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "EMR을 찾을 수 없습니다. 메인 창이 열려 있는지 확인해 주세요.");
                alert.showAndWait();
            }
        });

        HBox buttonBox = new HBox(10, btnCopy, btnSaveEmr);
        buttonBox.setPadding(new Insets(5, 0, 0, 0));

        root.getChildren().addAll(title, cbMsu, new Separator(), grid, btnCalc, lblResult, txtSummary, buttonBox);

        Scene scene = new Scene(root, 550, 800);
        stage.setTitle("Gout Diagnosis Calculator (ACR/EULAR)");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}