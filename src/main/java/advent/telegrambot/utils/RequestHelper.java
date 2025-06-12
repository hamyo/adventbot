package advent.telegrambot.utils;

import advent.telegrambot.classifier.DataType;
import advent.telegrambot.domain.dto.FileInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Comparator;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestHelper {
    private final TelegramClient telegramClient;

    public byte[] download(String fileId) {
        try {
            GetFile getFile = GetFile.builder()
                    .fileId(fileId)
                    .build();
            File fileInfo = telegramClient.execute(getFile);
            return IOUtils.toByteArray(telegramClient.downloadFileAsStream(fileInfo));
        } catch (Exception e) {
            log.error("Error on a getting file '{}'", fileId, e);
            throw new AppException("Не удалось загрузить файл. Пожалуйста, попробуйте, позже", e);
        }
    }


}
