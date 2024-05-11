package com.chronologic.domain;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

public abstract class MediaFile {

    public static final String FILE_PREFIX = "IMG_";
    private final File sourceFile;
    private final String originalDate;

    public MediaFile(File sourceFile, String originalDate) {
        this.sourceFile = sourceFile;
        this.originalDate = originalDate;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public String getAbsolutePath() {
        return sourceFile.getAbsolutePath();
    }

    public String getName() {
        return sourceFile.getName();
    }

    public String getFileExtension() {
        return FilenameUtils.getExtension(sourceFile.getName());
    }

    public abstract String getFileNamePostfix();

    public String getOriginalDate() {
        return originalDate;
    }

    public boolean isHeicFormat() {
        return getFileExtension().equalsIgnoreCase("HEIC");
    }

}
