package com.chronologic.domain;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

public class ImageFile extends MediaFile {

    private final String FILE_NAME_POSTFIX = "";

    public ImageFile(File sourceFile) {
        super(sourceFile);
    }

    @Override
    protected Date extractMediaCreationDate() throws ImageProcessingException, IOException {
        Metadata metadata = ImageMetadataReader.readMetadata(getSourceFile());
        ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        return directory == null ? null : directory.getDateOriginal();
    }

    @Override
    public String getFileNamePostfix() {
        return FILE_NAME_POSTFIX;
    }

}
