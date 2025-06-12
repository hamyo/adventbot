package advent.telegrambot.domain.dto;

import advent.telegrambot.utils.CheckLevel;

public record CheckError(CheckLevel level, String message) {
    public static CheckError error(String message) {
        return new CheckError(CheckLevel.ERROR, message);
    }

    public static CheckError warning(String message) {
        return new CheckError(CheckLevel.WARNING, message);
    }
}
