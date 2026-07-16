package com.emr.gds.features.bone.adapter.in.ui;

import com.emr.gds.features.bone.adapter.out.persistence.JdbcDexaRepository;
import com.emr.gds.features.bone.application.DexaDiagnosisService;
import com.emr.gds.features.bone.application.DexaReportService;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

public class DexaStage {

    public static void open() {
        Stage stage = new Stage();
        stage.setTitle("DEXA Bone Density Assessment");
        stage.setWidth(1200);
        stage.setHeight(800);

        // Initialize services
        JdbcDexaRepository repository = new JdbcDexaRepository();
        DexaDiagnosisService diagnosisService = new DexaDiagnosisService();
        DexaReportService reportService = new DexaReportService(repository, diagnosisService);

        // Create controller
        DexaController controller = new DexaController(reportService, diagnosisService);
        ScrollPane root = controller.getRoot();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
