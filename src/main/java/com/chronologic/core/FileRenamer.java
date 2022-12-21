package com.chronologic.core;


import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Pattern;
import com.chronologic.util.AppProperties;
import com.chronologic.util.DirectoryManager;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.mov.QuickTimeDirectory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.apache.tika.utils.StringUtils;


public class FileRenamer {

    private final Date DEFAULT_ZERO_DATE;
    private final String FILE_PREFIX = "IMG_";
    private final String DATE_FORMAT = "yyyyMMdd_HHmmss";

    private final Tika tika = new Tika();
    private final Pattern timePattern = Pattern.compile("\\d+");

    private boolean isSuccessful = false;
    private String time = "_000000";
    private String customDate;
    private String nameIncrement = "";
    private int counter = 2;

    {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        try {
            DEFAULT_ZERO_DATE = formatter.parse("12-12-1904");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


    public void setCustomDate(String customDate) {
        this.customDate = customDate;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }


    /**
     * Copies and renames a file by appending "IMG_" prefix and original file creation date to the name.
     * In case of Custom mode, the specified custom date is used. If the file is of video type,
     * the "_VID" postfix is appended to the name.
     *
     * @param mode renaming mode.
     */
    public void renameFiles(Mode mode) {
        isSuccessful = false;
        File mainDirectory = new File(DirectoryManager.getMainDirectoryPath());
        File[] filesToProcess = mainDirectory.listFiles();

        if (filesToProcess != null) {
            String outputFolderMainPath = DirectoryManager.getOutputFolderMainPath();
            createDirectory(outputFolderMainPath);

            if (mode == Mode.CUSTOM) {
                Arrays.sort(filesToProcess);
            }

            for (File file : filesToProcess) {

                if (file.isDirectory()) {
                    continue;
                }

                try {
                    String mimeType = tika.detect(file);

                    if (!mimeType.contains("image") && !mimeType.contains("video")) {
                        continue;
                    }

                    Date originalDate = extractMediaCreationDate(file, mimeType);
                    String filePostfix = determineFileNamePostfix(mimeType);
                    String newFilePathName;

                    if (mode == Mode.CUSTOM) {
                        newFilePathName = checkForNameCollision(mode, outputFolderMainPath, customDate, time,
                                filePostfix, FilenameUtils.getExtension(file.getName()));
                    } else {
                        if (originalDate != null && originalDate.after(DEFAULT_ZERO_DATE)) {
                            DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
                            newFilePathName = checkForNameCollision(mode, outputFolderMainPath,
                                    dateFormat.format(originalDate), StringUtils.EMPTY, filePostfix,
                                    FilenameUtils.getExtension(file.getName()));
                        } else {
                            createDirectory(DirectoryManager.getOutputFolderForEmptyFilesPath());
                            newFilePathName = DirectoryManager.getOutputFolderForEmptyFilesPath() + file.getName();
                        }
                    }

                    FileUtils.copyFile(file, new File(newFilePathName));

                } catch (IOException | ImageProcessingException ex) {
                    ChronoLogicRunner.getMainController()
                            .showErrorMessage(AppProperties.getAppProperty("file.processing.error.message"));
                    return;
                }
            }
            isSuccessful = true;
            openFolderInExplorer(outputFolderMainPath);

        } else {
            ChronoLogicRunner.getMainController()
                    .showErrorMessage(AppProperties.getAppProperty("empty.folder.error.message"));
        }
    }


    /**
     * Creates a directory by the specified path if it does not exist.
     *
     * @param dirPath path for a directory to create.
     */
    private void createDirectory(String dirPath) {
        Path path = Path.of(dirPath);

        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                ChronoLogicRunner.getMainController()
                        .showErrorMessage("The directory " + dirPath + " cannot be created.");
            }
        }
    }


    /**
     * Extracts the original creation/exposure date of the media.
     *
     * @param file     file to extract date from.
     * @param mimeType mime type of the file.
     * @return original media creation date if any exists.
     * @throws ImageProcessingException for general processing errors.
     * @throws IOException              for failed I/O operation.
     */
    private Date extractMediaCreationDate(File file, String mimeType) throws ImageProcessingException, IOException {
        Metadata metadata = ImageMetadataReader.readMetadata(file);

        if (mimeType.contains("image")) {
            ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            return directory.getDateOriginal();
        } else if (mimeType.contains("video")) {
            Directory directory = metadata.getFirstDirectoryOfType(Directory.class);
            return directory.getDate(QuickTimeDirectory.TAG_CREATION_TIME);
        }
        return null;
    }


    /**
     * Checks the original file name for the possible name collisions in the given folder
     * and adds an increment to a name if the collision occurs. Note: recursive method call.
     *
     * @param mode          renaming mode.
     * @param outputDir     output directory path.
     * @param date          media original creation date.
     * @param time          media creation time.
     * @param filePostfix   file name postfix.
     * @param fileExtension file extension.
     * @return unique file name within the given folder.
     */
    private String checkForNameCollision(Mode mode, String outputDir, String date,
                                         String time, String filePostfix, String fileExtension) {
        StringBuilder newFileName = new StringBuilder();

        if (mode == Mode.CUSTOM) {
            incrementTime();
        }

        newFileName.append(outputDir).append(FILE_PREFIX).append(date).append(time)
                .append(filePostfix).append(nameIncrement).append(".").append(fileExtension);
        Path path = Path.of(newFileName.toString());

        if (Files.exists(path)) {
            nameIncrement = "-" + counter++;
            return checkForNameCollision(mode, outputDir, date, time, filePostfix, fileExtension);
        } else {
            nameIncrement = "";
            counter = 2;
            return newFileName.toString();
        }
    }


    /**
     * Increments time postfix for a custom date by one second.
     */
    private void incrementTime() {
        time = timePattern.matcher(time)
                .replaceFirst(s -> String.format("%0" + s.group().length()
                        + "d", Integer.parseInt(s.group()) + 1));
    }


    private String determineFileNamePostfix(String mimeType) {
        if (mimeType.contains("video")) {
            return "_VID";
        }
        return StringUtils.EMPTY;
    }


    private void openFolderInExplorer(String folderPath) {
        try {
            Desktop.getDesktop()
                    .open(new File(folderPath));
        } catch (IOException e) {
            ChronoLogicRunner.getMainController()
                    .showErrorMessage(AppProperties.getAppProperty("folder.open.error.message"));
        }
    }

}
