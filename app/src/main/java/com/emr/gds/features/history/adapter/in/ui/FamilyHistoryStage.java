package com.emr.gds.features.history.adapter.in.ui;

import com.emr.gds.features.history.adapter.out.persistence.JdbcHistoryRepository;
import com.emr.gds.features.history.application.FamilyHistoryService;
import com.emr.gds.features.history.domain.HistoryRepository;
import com.emr.gds.input.IAITextAreaManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

public class FamilyHistoryStage {

    public static void open(IAITextAreaManager textAreaManager, Map<String, String> abbrevMap) {
        try {
            FXMLLoader loader = new FXMLLoader(FamilyHistoryStage.class.getResource("/fxml/family_history.fxml"));
            Parent root = loader.load();

            // Dependency Injection
            HistoryRepository repository = new JdbcHistoryRepository();
            FamilyHistoryService service = new FamilyHistoryService(repository);

            FamilyHistoryController controller = loader.getController();
            controller.setManagers(textAreaManager, abbrevMap, service);

            Stage stage = new Stage();
            stage.setTitle("Endocrinology - Family Medical History (JavaFX)");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}