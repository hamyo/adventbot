package advent.telegrambot.handler;

import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.service.AdventService;
import advent.telegrambot.service.PersonService;
import advent.telegrambot.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static advent.telegrambot.utils.MessageUtils.getTelegramUserId;


@Component
@RequiredArgsConstructor
public class HelloHandler implements MessageHandler {
    private final PersonService personService;
    private final AdventService adventService;
    private final TelegramClient telegramClient;

    @SneakyThrows
    @Override
    public void handle(Update update) {
        long chatId = MessageUtils.getChatId(update);
        Advent advent = adventService.findByChatId(chatId);
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(advent.getHelloMessage())
                .build();
        telegramClient.execute(message);
    }

    @Override
    public boolean canHandle(Update update) {
        return TelegramCommand.START.is(update) &&
                personService.isAdmin(getTelegramUserId(update));
    }
}
