package advent.telegrambot.utils;

public class NumberUtils {

    public static Short parseShort(String input, String fieldName) {
        try {
            return Short.valueOf(input.trim());
        } catch (Exception e) {
            throw new AppException("'" + fieldName + "' должно быть число. Сейчас:" + input, e);
        }
    }

}
