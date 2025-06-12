package advent.telegrambot;

import advent.telegrambot.handler.MessageHandlerFactory;
import advent.telegrambot.utils.AppException;
import advent.telegrambot.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;


@Component
@Slf4j
@RequiredArgsConstructor
public class Bot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    @Value("${telegram.bot.token}") private String token;
    private final TelegramClient telegramClient;
    private final MessageHandlerFactory handlerFactory;

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        try {
            try {
                handlerFactory.handle(update);
            } catch (AppException ex) {
                handleError(ex.getMessage(), update);
            }
        } catch (Exception e) {
            log.error("Error on getting message. ", e);
            handleError("Произошла неизвестная ошибка\uD83D\uDE2C. Повторите, пожалуйста, позже", update);
        }
    }

    private void handleError(String errorMessage, Update update) {
        SendMessage message = SendMessage // Create a message object
                .builder()
                .chatId(MessageUtils.getChatId(update))
                .text(errorMessage)
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error on sending error message. ", e);
        }
    }
}
