package advent.telegrambot.classifier;

import org.apache.commons.lang3.StringUtils;

public enum MimeType {
    OGG("audio/ogg"),
    MP3("audio/mpeg"),
    ;

    private final String name;

    MimeType(String name) {
        this.name = name;
    }

    public static String getPosibleExtension(String mimeType) {
        return StringUtils.isBlank(mimeType) ?
                "" :
                StringUtils.substringAfter(mimeType, "/");
    }
    public static String getPosibleExtensionWithDot(String mimeType) {
        String extension = getPosibleExtension(mimeType);
        return StringUtils.isBlank(mimeType) ? "" : "." + extension;
    }
}
