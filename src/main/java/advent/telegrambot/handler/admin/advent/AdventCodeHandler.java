package advent.telegrambot.handler.admin.advent;

import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.handler.MessageHandler;
import advent.telegrambot.handler.TelegramCommand;
import advent.telegrambot.handler.advent.AdventHandlerFactory;
import advent.telegrambot.service.AdventService;
import advent.telegrambot.service.PersonService;
import advent.telegrambot.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.ByteArrayInputStream;

@Component
@RequiredArgsConstructor
public class AdventCodeHandler implements MessageHandler {
    private final AdventService adventService;
    private final PersonService personService;
    private final TelegramClient telegramClient;
    private final AdventHandlerFactory adventHandlerFactory;

    @Override
    public void handle(Update update) {
        Long chatId = MessageUtils.getChatId(update);
        Advent advent = adventService.findByChatId(chatId);

        SendDocument message = SendDocument
                .builder()
                .chatId(chatId)
                .document(new InputFile(
                        new ByteArrayInputStream(adventService.getCodes(advent.getId())),
                        "Коды_к_адвенту.txt"))
                .replyMarkup(adventHandlerFactory.getAdminKeyboard(advent))
                .build();
        telegramClient.executeAsync(message);
    }

    @Override
    public boolean canHandle(Update update) {
        return TelegramCommand.ADVENTS_CODES.is(update) &&
                personService.isExist(MessageUtils.getTelegramUserId(update));
    }
}
