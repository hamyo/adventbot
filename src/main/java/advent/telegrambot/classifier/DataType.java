package advent.telegrambot.classifier;

import advent.telegrambot.domain.ClsDataType;
import advent.telegrambot.utils.NumberUtils;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Getter
public enum DataType {
    IMAGE(1, "изображение","image/jpeg", "jpg"),
    AUDIO(2, "аудиофайл", "mp3"),
    VOICE(3, "голосовое сообщение", "ogg"),
    VIDEO(4, "видео", "mp4"),
    TEXT(5, "текст", "txt"),
    GIF(6, "gif-анимация", "mp4"),
    DOCUMENT(7, "документ", null),
    ;

    private static final Map<Short, DataType> DATA_TYPE_MAP =
            Arrays.stream(DataType.values())
                    .collect(toMap(DataType::getId, Function.identity()));

    private final short id;
    private final String rusName;
    private final String defaultMimeType;
    private final String extension;

    DataType(int id, String rusName, String extension) {
        this(id, rusName, null, extension);
    }

    DataType(int id, String rusName, String defaultMimeType, String extension) {
        this.id = (short) id;
        this.rusName = rusName;
        this.defaultMimeType = defaultMimeType;
        this.extension = extension;
    }

    private static boolean isImage(String mimeType) {
        if (StringUtils.isBlank(mimeType)) {
            return false;
        }

        return mimeType.toLowerCase().startsWith("image");
    }

    public boolean is(ClsDataType dataType) {
        return dataType != null && id == dataType.getId();
    }

    public static DataType of(ClsDataType clsDataType) {
        if (clsDataType == null) {
            return null;
        }
        return DATA_TYPE_MAP.get(clsDataType.getId());
    }

    public static DataType of(Short id) {
        if (id == null) {
            return null;
        }
        return DATA_TYPE_MAP.get(id);
    }

    public static DataType of(Update update) {
        Message message = update.getMessage();
        if (message.hasVoice()) {
            return VOICE;
        } else if (message.hasVideo()) {
            return VIDEO;
        } else if (message.hasAudio()) {
            return AUDIO;
        } else if (message.hasPhoto()) {
            return IMAGE;
        } else if (message.hasAnimation()) {
            return GIF;
        } else if (message.hasDocument() && isImage(message.getDocument().getMimeType())) {
            return IMAGE;
        } else if (message.hasText()) {
            return TEXT;
        }

        return null;
    }

    public String getDefaultName() {
        return rusName.replace(" ", "_") +
                (StringUtils.isNotBlank(extension) ? "." + extension : "");
    }

    public static Set<Short> getIdsFromString(String input) {
        if (StringUtils.isBlank(input)) {
            return null;
        }

        return Arrays.stream(input.split("/|"))
                .filter(StringUtils::isNoneBlank)
                .map(type -> NumberUtils.parseShort(type, "Тип данных"))
                .map(DataType::of)
                .filter(Objects::nonNull)
                .map(DataType::getId)
                .collect(Collectors.toSet());

    }
}
