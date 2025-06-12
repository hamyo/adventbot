package advent.telegrambot.utils;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;

public class ResourceHelper {
    @SneakyThrows
    public static byte[] getContent(String path) {
        InputStream inputStream = ResourceHelper.class.getClassLoader().getResourceAsStream(path);
        return inputStream != null ? IOUtils.toByteArray(inputStream) : null;
    }
}
