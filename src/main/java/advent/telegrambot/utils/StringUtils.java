package advent.telegrambot.utils;

public class StringUtils {
    private final static String NOT_DEFINE_MESSAGE = "(не указано)";

    public static String getValueOrNotDefineMessage(Object value) {
        return value == null ? NOT_DEFINE_MESSAGE : value.toString();
    }
}
