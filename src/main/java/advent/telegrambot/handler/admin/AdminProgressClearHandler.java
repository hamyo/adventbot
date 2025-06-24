package advent.telegrambot.handler.admin;

import advent.telegrambot.handler.MessageHandler;
import advent.telegrambot.service.AdminProgressService;
import advent.telegrambot.service.PersonService;
import advent.telegrambot.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static advent.telegrambot.handler.TelegramCommand.ADMIN_CLEAR;

@Component
@RequiredArgsConstructor
public class AdminProgressClearHandler implements MessageHandler {
    private final TelegramClient telegramClient;
    private final AdminProgressService adminProgressService;
    private final PersonService personService;

    @SneakyThrows
    @Transactional
    @Override
    public void handle(Update update) {
        adminProgressService.delete(MessageUtils.getTelegramUserId(update));
        telegramClient.execute(
                SendMessage.builder()
                        .chatId(MessageUtils.getChatId(update))
                        .text("Прогресс администратора очищен.")
                        .build());
    }

    @Override
    public boolean canHandle(Update update) {
        return ADMIN_CLEAR.is(update) &&
                personService.isAdmin(MessageUtils.getTelegramUserId(update));
    }
}
