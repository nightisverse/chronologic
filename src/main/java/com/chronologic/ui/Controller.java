package com.chronologic.ui;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import com.chronologic.core.MediaFileRenamingTask;
import com.chronologic.core.Mode;
import com.chronologic.util.AppProperties;
import com.chronologic.util.DirectoryManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
    private Button browseButton;

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

    @FXML
    private CheckBox convertHeicCheckbox;


    public void showConvertHeicCheckbox() {
        convertHeicCheckbox.setVisible(true);
    }


    public void resetProgressIndicator() {
        if (isProgressIndicatorBound()) {
            unbindProgressIndicator();
        }
        progressIndicator.setProgress(0);
    }

    public void unbindProgressIndicator() {
        if (isProgressIndicatorBound()) {
            progressIndicator.progressProperty().unbind();
        }
    }


    private boolean isProgressIndicatorBound() {
        return progressIndicator.progressProperty().isBound();
    }


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
        resetProgressIndicator();

        if (file != null) {
            DirectoryManager.setMainDirectoryPath(file.getAbsolutePath());
            pathField.setText(file.getAbsolutePath());
            prepareUiControls();
        }
    }


    private void prepareUiControls() {
        enableModeRadioButtons();
        enableConvertHeicCheckBox();
        enableDatePicker();
        progressIndicator.setDisable(false);
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
            resetProgressIndicator();
        });

        int tooltipDuration = Integer.parseInt(AppProperties.getAppProperty("tooltip.duration.seconds"));
        nativeTooltip.setShowDuration(Duration.seconds(tooltipDuration));
        customTooltip.setShowDuration(Duration.seconds(tooltipDuration));
    }


    private void enableConvertHeicCheckBox() {
        convertHeicCheckbox.setDisable(false);
        convertHeicCheckbox.setSelected(false);

        convertHeicCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            resetProgressIndicator();
        });
    }


    private void enableDatePicker() {
        datePicker.setValue(null);
        datePicker.setPromptText("Select date");

        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            datePicker.setStyle(null);
        });
    }


    /**
     * Initiates file renaming process in the selected mode.
     *
     * @param event application event.
     */
    @FXML
    private void startRenamingProcess(ActionEvent event) {

        boolean performHeicConversion = convertHeicCheckbox.isSelected();

        MediaFileRenamingTask mediaFileRenamingTask = null;
        Mode selectedMode = Mode.getEnum(((RadioButton) modeGroup.getSelectedToggle()).getId());

        switch (selectedMode) {
            case NATIVE -> {
                mediaFileRenamingTask = new MediaFileRenamingTask(selectedMode, performHeicConversion);
            }
            case CUSTOM -> {
                String customDate = getCustomDate();

                if (customDate != null) {
                    mediaFileRenamingTask = new MediaFileRenamingTask(selectedMode, performHeicConversion, customDate);
                } else {
                    datePicker.setStyle("-fx-background-color: #D10000;");
                }
            }
        }

        if (mediaFileRenamingTask != null) {
            setUiElementsAsInteractiveTo(false);
            progressIndicator.progressProperty().bind(mediaFileRenamingTask.progressProperty());
            new Thread(mediaFileRenamingTask).start();
        }

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


    public void setUiElementsAsInteractiveTo(boolean flag) {
        browseButton.setMouseTransparent(!flag);
        renameButton.setMouseTransparent(!flag);
        nativeModeBtn.setMouseTransparent(!flag);
        customModeBtn.setMouseTransparent(!flag);
        datePicker.setMouseTransparent(!flag);
        convertHeicCheckbox.setMouseTransparent(!flag);
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