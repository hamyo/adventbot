package advent.telegrambot.utils;

import java.util.Arrays;

public enum MimeType {
    MP3("mp3", "audio/mpeg"),
    OGG("ogg", "audio/ogg");

    private final String extension;
    private final String mimeType;

    MimeType(String extension, String mimeType) {
        this.extension = extension;
        this.mimeType = mimeType;
    }

    public static String getMimeType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        return Arrays.stream(MimeType.values())
                .filter(type -> type.extension.equals(extension))
                .map(type -> type.mimeType)
                .findFirst()
                .orElse(null);
    }
}
