package com.chronologic.util;


import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DirectoryManager {

    private final static String OUTPUT_FOLDER_MAIN = File.separator
            + AppProperties.getAppProperty("output.folder.name.main") + File.separator;
    private final static String OUTPUT_FOLDER_FOR_EMPTY_FILES = File.separator
            + AppProperties.getAppProperty("output.folder.name.empty") + File.separator;

    private static String mainDirectoryPath;
    private static String outputFolderMainPath;
    private static String outputFolderForEmptyFilesPath;


    public static String getMainDirectoryPath() {
        return mainDirectoryPath;
    }


    public static void setMainDirectoryPath(String mainDirectoryPathToSet) {
        mainDirectoryPath = mainDirectoryPathToSet;
        outputFolderMainPath = mainDirectoryPath + OUTPUT_FOLDER_MAIN;
        outputFolderForEmptyFilesPath = mainDirectoryPath + OUTPUT_FOLDER_FOR_EMPTY_FILES;
    }


    public static String getOutputFolderMainPath() {
        return outputFolderMainPath;
    }


    public static String getOutputFolderForEmptyFilesPath() {
        return outputFolderForEmptyFilesPath;
    }


    public static void createDirectoryForRenamedFiles() throws RuntimeException {
        createDirectory(outputFolderMainPath);
    }


    public static void createDirectoryForEmptyFiles() throws RuntimeException {
        createDirectory(outputFolderForEmptyFilesPath);
    }


    public static void createDirectory(String dirPath) throws RuntimeException {
        Path path = Path.of(dirPath);

        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                throw new RuntimeException("The directory: " + dirPath + " cannot be created.");
            }
        }
    }


    public static void openFolderForRenamedFilesInExplorer() {
        openFolderInExplorer(outputFolderMainPath);
    }


    public static void openFolderInExplorer(String folderPath) {
        try {
            Desktop.getDesktop().open(new File(folderPath));
        } catch (IOException e) {
            ErrorHandler.showErrorMessageFromProperty("error.folder.open.message");
        }
    }

}

