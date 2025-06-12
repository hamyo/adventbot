package advent.telegrambot.utils;

import java.time.format.DateTimeFormatter;

public class DateUtils {
    private static final DateTimeFormatter RUS_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static DateTimeFormatter getRusDateFormatter() {
        return RUS_FORMATTER;
    }
}
