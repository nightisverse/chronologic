package com.chronologic.core;


import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Pattern;
import com.chronologic.domain.MediaFile;
import com.chronologic.domain.MediaFileFactory;
import com.chronologic.util.AppProperties;
import com.chronologic.util.DirectoryManager;
import com.chronologic.util.ErrorHandler;
import com.drew.imaging.ImageProcessingException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;
import org.apache.tika.utils.StringUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;


public class MediaFileRenamingTask extends Task<Void> {

    private final Date DEFAULT_ZERO_DATE;
    private final String DATE_FORMAT = "yyyyMMdd_HHmmss";
    private final Pattern timePattern = Pattern.compile("\\d+");

    private final Mode currentMode;
    private final boolean convertHeicToJpg;

    private String customDate;
    private String time = "_000001";
    private String nameIncrementPrefix = "";
    private int nameIncrementCounter = 2;

    {
        setOnSucceeded();
        setOnFailed();

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        try {
            DEFAULT_ZERO_DATE = formatter.parse("12-12-1904");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
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
        startRenamingProcess();
        return null;
    }


    /**
     * Renames all files in the main directory by copying them with new names.
     * Throws a runtime exception if the main directory is empty or cannot be read.
     */
    private void startRenamingProcess() {
        File mainDirectory = new File(DirectoryManager.getMainDirectoryPath());
        File[] filesToProcess = mainDirectory.listFiles();

        if (filesToProcess == null) {
            throw new RuntimeException(AppProperties.getAppProperty("error.empty.folder.message"));
        } else {
            sortFilesIfCustomMode(filesToProcess);
            copyFilesWithRenaming(filesToProcess);
        }
    }


    private void sortFilesIfCustomMode(File[] filesToProcess) {
        if (currentMode == Mode.CUSTOM) {
            Arrays.sort(filesToProcess);
        }
    }


    private void copyFilesWithRenaming(File[] filesToProcess) {
        BigDecimal renamingProgress = new BigDecimal("0.00");
        BigDecimal progressStep = BigDecimal.valueOf(Math.ceil((1.00 / filesToProcess.length) * 100.0) / 100.0);

        try {
            DirectoryManager.createDirectoryForRenamedFiles();

            for (File file : filesToProcess) {
                renamingProgress = renamingProgress.add(progressStep);
                updateProgress(renamingProgress.doubleValue(), 1.00);

                if (file.isDirectory()) {
                    continue;
                }

                MediaFile mediaFile = MediaFileFactory.createMediaFile(file);

                if (mediaFile == null) {
                    continue;
                }

                String newFileAbsolutePath = getNewFileAbsolutePath(mediaFile);
                copyFileWithRenaming(mediaFile, newFileAbsolutePath);
            }

        } catch (IOException ex) {
            throw new MediaFileProcessingException("error.file.processing.message");
        } finally {
            ChronoLogicRunner.getMainController().setUiElementsAsInteractiveTo(true);
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
        try {
            Date creationDate = mediaFile.getCreationDate();

            if (isCreationDateValid(creationDate)) {
                DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
                String originalCreationDate = dateFormat.format(creationDate);
                return getNewNonConflictingFileName(mediaFile, originalCreationDate, StringUtils.EMPTY);
            } else {
                DirectoryManager.createDirectoryForEmptyFiles();
                return DirectoryManager.getOutputFolderForEmptyFilesPath() + mediaFile.getName();
            }

        } catch (IOException | ImageProcessingException ex) {
            throw new MediaFileProcessingException("error.file.processing.message");
        }
    }


    private boolean isCreationDateValid(Date creationDate) {
        return creationDate != null && creationDate.after(DEFAULT_ZERO_DATE);
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
