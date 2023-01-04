package com.chronologic.domain;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import com.drew.imaging.ImageProcessingException;
import org.apache.commons.io.FilenameUtils;

public abstract class MediaFile {

    public static final String FILE_PREFIX = "IMG_";
    private final File sourceFile;
    private Date creationDate;

    public MediaFile(File sourceFile) {
        this.sourceFile = sourceFile;
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

    public Date getCreationDate() throws ImageProcessingException, IOException {
        if (creationDate == null) {
            creationDate = extractMediaCreationDate();
        }
        return creationDate;
    }

    protected abstract Date extractMediaCreationDate() throws ImageProcessingException, IOException;

    public boolean isHeicFormat() {
        return getFileExtension().equalsIgnoreCase("HEIC");
    }

}
