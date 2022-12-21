package com.chronologic.util;


import java.io.File;

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

}

