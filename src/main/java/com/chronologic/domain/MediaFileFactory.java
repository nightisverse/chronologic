package com.chronologic.domain;

import java.io.File;

public class MediaFileFactory {

    private MediaFileFactory() {
    }

    public static MediaFile createMediaFile(File file, MimeType mimeType, String originalDate, String creationDate) {
        String captureDate = (originalDate != null && !originalDate.equals("-")) ? originalDate : creationDate;
        switch (mimeType) {
            case IMAGE -> {
                return new ImageFile(file, captureDate);
            }
            case VIDEO -> {
                return new VideoFile(file, captureDate);
            }
            default -> throw new UnsupportedOperationException("Unsupported mime type: " + mimeType);
        }
    }

}
