package com.emr.gds.features.clinicalLab;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClinicalLabLauncher extends Application {

    private static final Logger logger = LoggerFactory.getLogger(ClinicalLabLauncher.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/emr/gds/features/clinicalLab/main.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Clinical Lab Items");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void open() {
        try {
            new ClinicalLabLauncher().start(new Stage());
        } catch (Exception e) {
            logger.error("ClinicalLab 화면 열기 실패", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
