package advent.telegrambot;

import advent.telegrambot.classifier.DataType;
import advent.telegrambot.domain.Content;
import advent.telegrambot.domain.dto.FileInfo;
import advent.telegrambot.utils.AppException;
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
@Slf4j
@RequiredArgsConstructor
public class FileHelper {
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

    public String getFileId(Update update) {
        if (!update.hasMessage()) {
            return null;
        }

        Message message = update.getMessage();
        if (message.hasDocument()) {
            return message.getDocument().getFileId();
        } else if (message.hasAudio()) {
            return message.getAudio().getFileId();
        } else if (message.hasVideo()) {
            return message.getVideo().getFileId();
        } else if (message.hasVoice()) {
            return message.getVoice().getFileId();
        } else if (message.hasPhoto()) {
            return message.getPhoto().stream()
                    .max(Comparator.comparing(PhotoSize::getFileSize))
                    .map(PhotoSize::getFileId)
                    .orElse(null);
        }

        return null;
    }

    public Optional<Content> getContent(Update update) {
        if (!update.hasMessage()) {
            return Optional.empty();
        }

        Message message = update.getMessage();
        if (message.hasDocument()) {
            Document document = message.getDocument();

            return Optional.of(
                    new Content(
                            DataType.DOCUMENT,
                            document.getFileName(),
                            message.getCaption(),
                            download(document.getFileId())));
        } else if (message.hasAudio()) {
            return Optional.of(new Content(
                    DataType.AUDIO,
                    message.getAudio().getFileName(),
                    message.getCaption(),
                    download(message.getAudio().getFileId())));
        } else if (message.hasVideo()) {
            return Optional.of(new Content(
                    DataType.VIDEO,
                    message.getVideo().getFileName(),
                    message.getCaption(),
                    download(message.getVideo().getFileId())));
        } else if (message.hasVoice()) {
            return Optional.of(new Content(
                    DataType.VOICE,
                    null,
                    message.getCaption(),
                    download(message.getVoice().getFileId())));
        } else if (message.hasPhoto()) {
            return message.getPhoto().stream()
                    .max(Comparator.comparing(PhotoSize::getFileSize))
                    .map(fileSize -> new Content(
                            DataType.IMAGE,
                            null,
                            message.getCaption(),
                            download(fileSize.getFileId())));
        }

        return Optional.empty();
    }
}
