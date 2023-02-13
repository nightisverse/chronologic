package com.chronologic.domain;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.mov.QuickTimeDirectory;

public class VideoFile extends MediaFile {

    public final String FILE_NAME_POSTFIX = "_VID";

    public VideoFile(File sourceFile) {
        super(sourceFile);
    }

    @Override
    protected Date extractMediaCreationDate() throws ImageProcessingException, IOException {
        Metadata metadata = ImageMetadataReader.readMetadata(getSourceFile());
        Directory directory = metadata.getFirstDirectoryOfType(Directory.class);
        return directory == null ? null : directory.getDate(QuickTimeDirectory.TAG_CREATION_TIME);
    }

    @Override
    public String getFileNamePostfix() {
        return FILE_NAME_POSTFIX;
    }

}
