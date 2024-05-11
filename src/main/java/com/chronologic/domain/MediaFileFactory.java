package com.chronologic.domain;

import java.io.File;

public class MediaFileFactory {

    private MediaFileFactory() {
    }

    public static MediaFile createMediaFile(File file, MimeType mimeType, String originalDate) {
        switch (mimeType) {
            case IMAGE -> {
                return new ImageFile(file, originalDate);
            }
            case VIDEO -> {
                return new VideoFile(file, originalDate);
            }
            default -> throw new UnsupportedOperationException("Unsupported mime type: " + mimeType);
        }
    }

}
