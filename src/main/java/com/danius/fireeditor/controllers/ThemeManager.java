package com.danius.fireeditor.controllers;

import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ThemeManager {
    private static final ThemeManager INSTANCE = new ThemeManager();
    private static final String DARK_CSS = ThemeManager.class.getResource("/com/danius/fireeditor/dark-theme.css").toExternalForm();
    private static final Image APP_ICON;

    static {
        Image icon = null;
        try {
            var iconUrl = ThemeManager.class.getResource("/com/danius/fireeditor/fire-emblem-awakening.png");
            if (iconUrl != null) {
                icon = new Image(iconUrl.toExternalForm());
            }
        } catch (Exception ignored) {
        }
        APP_ICON = icon;
    }

    private boolean darkMode = true;

    private ThemeManager() {
    }

    public static ThemeManager getInstance() {
        return INSTANCE;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean enabled) {
        if (darkMode == enabled) return;
        darkMode = enabled;
        applyToAllWindows();
    }

    public void toggleDarkMode() {
        setDarkMode(!darkMode);
    }

    public void applyToDialogPane(DialogPane dialogPane) {
        if (dialogPane == null) return;
        dialogPane.getStylesheets().remove(DARK_CSS);
        if (darkMode) {
            dialogPane.getStylesheets().add(DARK_CSS);
        }
    }

    public void applyToScene(Scene scene) {
        if (scene == null) return;
        scene.getStylesheets().remove(DARK_CSS);
        if (darkMode) {
            scene.getStylesheets().add(DARK_CSS);
        }
    }

    public void applyToAllWindows() {
        for (Window window : Window.getWindows()) {
            if (window.getScene() != null) {
                applyToScene(window.getScene());
            }
        }
    }

    public void setStageIcon(Stage stage) {
        if (stage == null || APP_ICON == null) return;
        stage.getIcons().add(APP_ICON);
    }

    public void applyToStage(Stage stage) {
        if (stage == null) return;
        if (stage.getScene() != null) {
            applyToScene(stage.getScene());
        }
        setStageIcon(stage);
    }
}
