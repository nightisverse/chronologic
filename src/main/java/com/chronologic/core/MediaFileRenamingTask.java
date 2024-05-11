package com.chronologic.core;

import com.chronologic.domain.MediaFile;
import com.chronologic.util.AppProperties;
import com.chronologic.util.DirectoryManager;
import com.chronologic.util.ErrorHandler;
import com.chronologic.util.ExifTool;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;


public class MediaFileRenamingTask extends Task<Void> {

    private final Mode currentMode;
    private final boolean convertHeicToJpg;
    private final Pattern timePattern = Pattern.compile("\\d+");

    private String customDate;
    private String time = "_000001";
    private String nameIncrementPrefix = "";
    private int nameIncrementCounter = 2;

    {
        setOnSucceeded();
        setOnFailed();
    }

    public MediaFileRenamingTask(Mode currentMode, boolean convertHeicToJpg) {
        this.currentMode = currentMode;
        this.convertHeicToJpg = convertHeicToJpg;
    }


    public MediaFileRenamingTask(Mode currentMode, boolean convertHeicToJpg, String customDate) {
        this.currentMode = currentMode;
        this.convertHeicToJpg = convertHeicToJpg;
        this.customDate = customDate;
    }


    @Override
    protected Void call() {
        try {
            initializeProgressIndicator();
            startRenamingProcess();
            return null;
        } finally {
            ChronoLogicRunner.getMainController().setUiElementsAsInteractiveTo(true);
        }
    }

    private void initializeProgressIndicator() {
        double start = 0.00;
        double end = 1.00;
        updateProgress(start, end);
    }

    /**
     * Renames all files in the main directory by copying them with new names.
     * Throws a runtime exception if the main directory is empty or cannot be read.
     */
    private void startRenamingProcess() {
        ExifTool exifTool = new ExifTool(DirectoryManager.getMainDirectoryPath());
        List<MediaFile> mediaFiles = exifTool.runExifTool();

        if (mediaFiles.isEmpty()) {
            throw new RuntimeException(AppProperties.getAppProperty("error.empty.folder.message"));
        } else {
            copyFilesWithRenaming(mediaFiles);
        }
    }

    private void copyFilesWithRenaming(List<MediaFile> filesToProcess) {
        BigDecimal renamingProgress = new BigDecimal("0.00");
        BigDecimal progressStep = BigDecimal.valueOf(Math.ceil((1.00 / filesToProcess.size()) * 100.0) / 100.0);

        try {
            DirectoryManager.createDirectoryForRenamedFiles();

            for (MediaFile mediaFile : filesToProcess) {
                renamingProgress = renamingProgress.add(progressStep);
                updateProgress(renamingProgress.doubleValue(), 1.00);

                String newFileAbsolutePath = getNewFileAbsolutePath(mediaFile);
                copyFileWithRenaming(mediaFile, newFileAbsolutePath);
            }
        } catch (IOException ex) {
            throw new MediaFileProcessingException("error.file.processing.message");
        }
    }


    private String getNewFileAbsolutePath(MediaFile mediaFile) {
        String newFileAbsolutePath;

        if (currentMode == Mode.CUSTOM) {
            newFileAbsolutePath = getNewNonConflictingFileName(mediaFile, customDate, time);
        } else {
            newFileAbsolutePath = getNewFileAbsolutePathForNativeMode(mediaFile);
        }

        return newFileAbsolutePath;
    }


    private String getNewFileAbsolutePathForNativeMode(MediaFile mediaFile) {
        String originalDate = mediaFile.getOriginalDate();

        if (isOriginalDateValid(originalDate)) {
            return getNewNonConflictingFileName(mediaFile, originalDate, "");
        } else {
            DirectoryManager.createDirectoryForEmptyFiles();
            String newFileAbsolutePath = DirectoryManager.getOutputFolderForEmptyFilesPath() + mediaFile.getName();

            return convertHeicToJpg && mediaFile.isHeicFormat()
                    ? updateFileNameExtension(newFileAbsolutePath, "JPG")
                    : newFileAbsolutePath;
        }
    }


    private boolean isOriginalDateValid(String originalDate) {
        return originalDate != null && !originalDate.equals("-");
    }


    private String updateFileNameExtension(String fileName, String newExtension) {
        int extensionStartIndex = fileName.lastIndexOf(".") + 1;
        String fileNameWithoutExtension = fileName.substring(0, extensionStartIndex);
        return fileNameWithoutExtension + newExtension;
    }


    /**
     * Generates a new file name for the given media file based on the capture date and time.
     * If the generated name conflicts with an existing file name in the given folder,
     * increments the name and tries again.
     * Note: recursive method call is used.
     *
     * @param mediaFile media file object.
     * @param date      media original creation date.
     * @param time      media creation time.
     * @return unique file name within the given folder.
     */
    private String getNewNonConflictingFileName(MediaFile mediaFile, String date, String time) {
        String fileExtension = convertHeicToJpg && mediaFile.isHeicFormat() ? "JPG" : mediaFile.getFileExtension();
        StringBuilder newFileName = new StringBuilder();

        newFileName.append(DirectoryManager.getOutputFolderMainPath())
                .append(MediaFile.FILE_PREFIX)
                .append(date)
                .append(time)
                .append(mediaFile.getFileNamePostfix())
                .append(nameIncrementPrefix)
                .append(".")
                .append(fileExtension);

        Path newFilePath = Path.of(newFileName.toString());

        if (Files.exists(newFilePath)) {
            increaseNameIncrement();
            return getNewNonConflictingFileName(mediaFile, date, time);
        } else {
            incrementTimeByOneSecond();
            resetNameIncrement();
            return newFileName.toString();
        }
    }


    private void copyFileWithRenaming(MediaFile mediaFile, String newFileAbsolutePath) throws IOException {
        if (convertHeicToJpg && mediaFile.isHeicFormat()) {
            convertHeicToJpg(mediaFile, newFileAbsolutePath);
        } else {
            FileUtils.copyFile(mediaFile.getSourceFile(), new File(newFileAbsolutePath));
        }
    }


    private void convertHeicToJpg(MediaFile mediaFile, String outputFileName) {
        try {
            String imageMagickPath = AppProperties.getAppProperty("image.magick.path");

            ConvertCmd cmd = new ConvertCmd();
            cmd.setSearchPath(imageMagickPath);

            IMOperation operation = new IMOperation();
            operation.addImage(mediaFile.getAbsolutePath());
            operation.addImage(outputFileName);

            cmd.run(operation);
        } catch (IOException | InterruptedException | IM4JavaException ex) {
            throw new MediaFileProcessingException("error.converting.failure");
        }
    }


    private void incrementTimeByOneSecond() {
        time = timePattern.matcher(time)
                .replaceFirst(s -> String.format("%0" + s.group().length()
                        + "d", Integer.parseInt(s.group()) + 1));
    }


    private void increaseNameIncrement() {
        nameIncrementPrefix = "-" + nameIncrementCounter++;
    }


    private void resetNameIncrement() {
        nameIncrementPrefix = "";
        nameIncrementCounter = 2;
    }


    private void setOnSucceeded() {
        setOnSucceeded(event -> DirectoryManager.openFolderForRenamedFilesInExplorer());
    }


    private void setOnFailed() {
        setOnFailed(event -> {
            Platform.runLater(() -> {
                ErrorHandler.showErrorMessage(getException().getMessage());
            });
        });
    }

}
