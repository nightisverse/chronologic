package com.chronologic.domain;

import java.io.File;

public class ImageFile extends MediaFile {

    private final String FILE_NAME_POSTFIX = "";

    public ImageFile(File sourceFile, String originalDate) {
        super(sourceFile, originalDate);
    }

    @Override
    public String getFileNamePostfix() {
        return FILE_NAME_POSTFIX;
    }

}
