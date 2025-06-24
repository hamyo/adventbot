package advent.telegrambot.handler.admin.advent;

import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.handler.MessageHandler;
import advent.telegrambot.handler.TelegramCommand;
import advent.telegrambot.handler.advent.AdventHandlerFactory;
import advent.telegrambot.service.AdventService;
import advent.telegrambot.service.PersonService;
import advent.telegrambot.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.ByteArrayInputStream;

import static advent.telegrambot.handler.TelegramCommand.ADVENTS_CODES;
import static advent.telegrambot.handler.TelegramCommand.ADVENTS_PERSONS;

@Component
@RequiredArgsConstructor
public class AdventCodeHandler implements MessageHandler {
    private final AdventService adventService;
    private final PersonService personService;
    private final TelegramClient telegramClient;
    private final AdventHandlerFactory adventHandlerFactory;

    @SneakyThrows
    @Override
    public void handle(Update update) {
        Integer adventId = ADVENTS_CODES.getIdFromAction(MessageUtils.getMessageText(update));
        Advent advent = adventService.findById(adventId);

        SendDocument message = SendDocument.builder()
                .chatId(MessageUtils.getChatId(update))
                .document(new InputFile(
                        new ByteArrayInputStream(adventService.getCodes(advent.getId())),
                        "Коды_к_адвенту.txt"))
                .replyMarkup(adventHandlerFactory.getAdminKeyboard(advent))
                .build();
        telegramClient.execute(message);
    }

    @Override
    public boolean canHandle(Update update) {
        return TelegramCommand.ADVENTS_CODES.is(update) &&
                personService.isAdmin(MessageUtils.getTelegramUserId(update));
    }
}
