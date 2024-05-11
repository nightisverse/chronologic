package com.chronologic.domain;

import java.io.File;

public class VideoFile extends MediaFile {

    public final String FILE_NAME_POSTFIX = "_VID";

    public VideoFile(File sourceFile, String originalDate) {
        super(sourceFile, originalDate);
    }

    @Override
    public String getFileNamePostfix() {
        return FILE_NAME_POSTFIX;
    }

}
