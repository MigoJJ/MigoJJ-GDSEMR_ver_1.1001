package com.emr.gds.features.template;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Consumer;

public class TemplateEditStage {

    private static final Logger logger = LoggerFactory.getLogger(TemplateEditStage.class);

    public static void open(Consumer<String> onTemplateSelected) {
        try {
            FXMLLoader loader = new FXMLLoader(TemplateEditStage.class.getResource("/fxml/template_editor.fxml"));
            Parent root = loader.load();

            TemplateEditController controller = loader.getController();
            // Initialize repository and callback
            controller.setRepository(new TemplateRepository());
            controller.setOnUseCallback(onTemplateSelected);

            Stage stage = new Stage();
            stage.setTitle("EMR Template Editor (JavaFX)");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Make it modal
            stage.show();

        } catch (IOException e) {
            logger.error("템플릿 편집기 화면 열기 실패", e);
        }
    }
}
