package advent.telegrambot.handler;

import advent.telegrambot.service.AdventService;
import advent.telegrambot.service.PersonService;
import advent.telegrambot.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@RequiredArgsConstructor
public class SetChatIdHandler implements MessageHandler {
    private final TelegramClient telegramClient;
    private final PersonService personService;
    private final AdventService adventService;


    @Override
    @SneakyThrows
    public void handle(Update update) {
        Long chatId = MessageUtils.getChatId(update);
        adventService.setChatId(chatId);
        telegramClient.executeAsync(
                SendMessage.builder()
                        .chatId(chatId)
                        .text("Id чата успешно установлено для адвента")
                        .build()
        );
    }

    @Override
    public boolean canHandle(Update update) {
        return TelegramCommand.SET_CHAT_ID.is(update) &&
                personService.isAdmin(MessageUtils.getTelegramUserId(update));
    }
}
