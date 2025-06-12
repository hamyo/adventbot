package advent.telegrambot.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SerializeHelper {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static ObjectMapper getMapper() {
        return mapper;
    }
}
