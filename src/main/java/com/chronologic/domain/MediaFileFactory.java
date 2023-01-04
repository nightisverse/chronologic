package com.chronologic.domain;

import java.io.File;
import java.io.IOException;
import org.apache.tika.Tika;

public class MediaFileFactory {

    private static final Tika tika = new Tika();

    private MediaFileFactory() {

    }

    public static MediaFile createMediaFile(File file) throws IOException {
        String mimeType = tika.detect(file);
        if (mimeType.contains("image")) {
            return new ImageFile(file);
        } else if (mimeType.contains("video")) {
            return new VideoFile(file);
        } else {
            return null;
        }
    }

}
