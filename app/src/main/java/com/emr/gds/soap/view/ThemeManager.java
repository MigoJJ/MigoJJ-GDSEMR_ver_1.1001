package com.emr.gds.soap.view;

import javafx.scene.Scene;

import java.util.Objects;

public class ThemeManager {

    public enum Theme {
        CLINICAL("/themes/clinical.css"),
        VAN_GOGH("/themes/van-gogh.css"), // Placeholder
        DARK_MODE("/themes/dark.css");   // Placeholder

        private final String cssPath;

        Theme(String cssPath) {
            this.cssPath = cssPath;
        }

        public String getCssPath() {
            return cssPath;
        }
    }

    public void applyTheme(Scene scene, Theme theme) {
        Objects.requireNonNull(scene, "Scene cannot be null");
        Objects.requireNonNull(theme, "Theme cannot be null");

        // Clear existing stylesheets to apply only the new theme
        scene.getStylesheets().clear();
        
        String cssResource = getClass().getResource(theme.getCssPath()).toExternalForm();
        scene.getStylesheets().add(cssResource);
    }
}
