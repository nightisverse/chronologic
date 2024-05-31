package com.chronologic.util;

import com.chronologic.domain.MediaFile;
import com.chronologic.domain.MediaFileFactory;
import com.chronologic.domain.MimeType;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ExifTool {

    private final File exifToolLocation;
    private final String mediaFolderPath;

    public ExifTool(String mediaFolderPath) {
        this.exifToolLocation = new File(AppProperties.getAppProperty("exif.tool.path"));
        this.mediaFolderPath = mediaFolderPath;
    }


    public List<MediaFile> runExifTool() {
        StringBuilder command = new StringBuilder();
        command.append("exiftool")
                .append(" -p \"$filename:$mimeType:$dateTimeOriginal:$creationDate\"")
                .append(" -d \"%Y%d%m_%H%M%S\"")
                .append(" -q -f -m")
                .append(" ")
                .append("\"")
                .append(this.mediaFolderPath)
                .append("\"");

        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(this.exifToolLocation);
        builder.command("cmd.exe", "/c", command.toString());

        try {
            Process process = builder.start();
            InputStream inputStream = process.getInputStream();
            List<MediaFile> mediaFiles = getProcessedSupportedFiles(inputStream);

            boolean isFinished = process.waitFor(5, TimeUnit.SECONDS);

            if (!isFinished) {
                process.destroyForcibly();
            }

            return mediaFiles;
        } catch (IOException | InterruptedException ex) {
            throw new ExifToolException(ex.getMessage());
        }
    }


    private List<MediaFile> getProcessedSupportedFiles(InputStream inputStream) {
        List<MediaFile> mediaFiles = new ArrayList<>();

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String mediaFileInfo;
            while ((mediaFileInfo = bufferedReader.readLine()) != null) {
                String[] metadata = mediaFileInfo.split(":");
                String name = metadata[0];
                String mimeType = metadata[1];
                String originalDate = metadata[2];
                String creationDate = metadata[3];

                if (name.equals("-") || !MimeType.isSupportedType(mimeType)) {
                    continue;
                }

                File file = new File(this.mediaFolderPath + File.separator + name);
                MediaFile mediaFile = MediaFileFactory
                        .createMediaFile(file, MimeType.getEnum(mimeType), originalDate, creationDate);
                mediaFiles.add(mediaFile);
            }
        } catch (IOException ex) {
            throw new ExifToolException(ex.getMessage());
        }

        return mediaFiles;
    }

}
