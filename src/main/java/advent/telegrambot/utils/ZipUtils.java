package advent.telegrambot.utils;

import lombok.SneakyThrows;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
    @SneakyThrows
    public static void addFileToZip(ZipOutputStream zos, String fileName, byte[] content) {
        // Создаем новую запись в ZIP-архиве
        ZipEntry entry = new ZipEntry(fileName);
        zos.putNextEntry(entry);

        // Записываем содержимое файла
        zos.write(content);

        // Закрываем текущую запись
        zos.closeEntry();
    }
}
