package com.emr.gds.features.ekg;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class EkgReportStage {

    private static final Logger logger = LoggerFactory.getLogger(EkgReportStage.class);

    public static void open() {
        try {
            FXMLLoader loader = new FXMLLoader(EkgReportStage.class.getResource("/fxml/ekg_report.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("EMR EKG Analysis (JavaFX)");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            logger.error("EKG 보고서 화면 열기 실패", e);
        }
    }
}
