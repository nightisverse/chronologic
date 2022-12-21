package com.chronologic.ui;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import com.chronologic.core.FileRenamer;
import com.chronologic.core.Mode;
import com.chronologic.util.AppProperties;
import com.chronologic.util.DirectoryManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Controller {

    @FXML
    private AnchorPane mainAnchorPane;

    @FXML
    private TextField pathField;

    @FXML
    private Button renameButton;

    @FXML
    private RadioButton nativeModeBtn;

    @FXML
    private RadioButton customModeBtn;

    @FXML
    private ToggleGroup modeGroup;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Tooltip nativeTooltip;

    @FXML
    private Tooltip customTooltip;

    /**
     * Opens directory chooser dialog window, if the directory is selected,
     * sets the path to the directory text field and enables all the application elements.
     *
     * @param event application event.
     */
    @FXML
    private void browseDirectoryButtonClick(ActionEvent event) {

        DirectoryChooser directoryChooser = new DirectoryChooser();
        Stage stage = (Stage) mainAnchorPane.getScene().getWindow();
        File file = directoryChooser.showDialog(stage);
        progressIndicator.setProgress(0);

        if (file != null) {
            DirectoryManager.setMainDirectoryPath(file.getAbsolutePath());
            pathField.setText(file.getAbsolutePath());

            enableModeRadioButtons();

            datePicker.setValue(null);
            datePicker.setPromptText("Select date");

            datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
                datePicker.setStyle(null);
            });

            progressIndicator.setDisable(false);
        }
    }


    /**
     * Initiates file renaming process in the selected mode.
     *
     * @param event application event.
     */
    @FXML
    private void startRenameProcess(ActionEvent event) {

        FileRenamer fileRenamer = new FileRenamer();

        switch (Mode.getEnum(((RadioButton) modeGroup.getSelectedToggle()).getId())) {
            case NATIVE -> fileRenamer.renameFiles(Mode.NATIVE);
            case CUSTOM -> {
                String customDate = getCustomDate();

                if (customDate != null) {
                    fileRenamer.setCustomDate(customDate);
                    fileRenamer.renameFiles(Mode.CUSTOM);
                } else {
                    datePicker.setStyle("-fx-background-color: #D10000;");
                }
            }
        }

        if (fileRenamer.isSuccessful()) {
            progressIndicator.setProgress(100);
        }
    }

    /**
     * Enables radio-button elements setting them in the deselected state.
     * Adds listener to control date-picker state depending on the selected radio-button.
     */
    private void enableModeRadioButtons() {
        nativeModeBtn.setDisable(false);
        customModeBtn.setDisable(false);

        nativeModeBtn.setSelected(false);
        customModeBtn.setSelected(false);

        modeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (renameButton.isDisable()) {
                renameButton.setDisable(false);
            }
            datePicker.setDisable(!customModeBtn.isSelected());
            datePicker.setStyle(null);
            progressIndicator.setProgress(0);
        });

        int tooltipDuration = Integer.parseInt(AppProperties.getAppProperty("tooltip.duration.seconds"));
        nativeTooltip.setShowDuration(Duration.seconds(tooltipDuration));
        customTooltip.setShowDuration(Duration.seconds(tooltipDuration));
    }


    /**
     * Gets a date value from date-picker if any and formats it.
     *
     * @return String value of a date in "yyyyMMdd" format.
     */
    private String getCustomDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        if (datePicker.getValue() != null) {
            return dateFormat.format(Date.from(datePicker.getValue()
                    .atStartOfDay(ZoneId.systemDefault()).toInstant()));
        } else {
            return null;
        }
    }


    /**
     * Displays error dialog pop-up with the specified error message.
     *
     * @param message error message to display.
     */
    public void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(mainAnchorPane.getScene().getWindow());
        alert.setTitle(AppProperties.getAppProperty("error.dialog.popup.title"));
        alert.setHeaderText(AppProperties.getAppProperty("error.dialog.popup.header.text"));
        alert.setContentText(message);
        alert.showAndWait();
    }

}