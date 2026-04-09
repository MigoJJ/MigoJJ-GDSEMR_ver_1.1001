package com.emr.gds.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);

    public enum ErrorContext {
        INITIALIZATION,
        SAVE_OPERATION,
        COPY_OPERATION,
        ABBREVIATION_EXPANSION,
        EMRFMH_OPEN,
        OTHER
    }

    public void handle(Throwable e, ErrorContext context, String userFacingMessage, Optional<Runnable> retryAction) {
        // 1. Logging
        logger.error("Error in {}: {} - {}", context.name(), userFacingMessage, e.getMessage(), e);

        // 2. User-friendly message and recovery options
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(I18N.get("error.occurred")); // Use I18N
            alert.setHeaderText(I18N.get("error.unexpected") + userFacingMessage); // Use I18N
            alert.setContentText(e.getClass().getSimpleName() + ": " + (e.getMessage() != null ? e.getMessage() : I18N.get("error.no_details"))); // Use I18N

            if (retryAction.isPresent()) {
                ButtonType retry = new ButtonType(I18N.get("error.button.retry"), ButtonBar.ButtonData.OK_DONE); // Use I18N
                ButtonType cancel = new ButtonType(I18N.get("error.button.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE); // Use I18N
                alert.getButtonTypes().setAll(retry, cancel);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == retry) {
                    retryAction.get().run();
                }
            } else {
                alert.getButtonTypes().setAll(ButtonType.OK);
                alert.showAndWait();
            }
        });
    }

    public void showInfo(String header, String content) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setHeaderText(header);
            a.setContentText(content);
            a.showAndWait();
        });
    }

    // Simplified handle without retry for common cases
    public void handle(Throwable e, ErrorContext context, String userFacingMessage) {
        handle(e, context, userFacingMessage, Optional.empty());
    }
}