package com.chronologic.core;

import com.chronologic.ui.Controller;
import com.chronologic.util.AppProperties;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;


public class ChronoLogicRunner extends Application {

    private static Controller mainController;

    public static Controller getMainController() {
        return mainController;
    }

    public static void main(String[] args) {
        launch();
    }


    /**
     * Invokes the error dialog with the uncaught exception message.
     *
     * @param thread    thread the application is running in.
     * @param exception uncaught exception.
     */
    private static void showError(Thread thread, Throwable exception) {
        mainController.showErrorMessage(exception.getMessage());
    }


    @Override
    public void start(Stage stage) throws IOException {
        Thread.setDefaultUncaughtExceptionHandler(ChronoLogicRunner::showError);
        URL fxmlLocation = getClass().getResource("/com/chronologic/chronologic-view.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Scene scene = new Scene(fxmlLoader.load(), 795, 535);
        mainController = fxmlLoader.getController();
        includeHeicConverterIfImageMagicIsBundled();
        stage.getIcons().add(new Image("/camera_icon.png"));
        stage.setTitle("ChronoLogic v2");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }


    private void includeHeicConverterIfImageMagicIsBundled() {
        if (isImageMagickBundled()) {
            mainController.showConvertHeicCheckbox();
        }
    }


    private boolean isImageMagickBundled() {
        Path imageMagickPath = Path.of(AppProperties.getAppProperty("image.magick.path"));
        return Files.exists(imageMagickPath);
    }

}