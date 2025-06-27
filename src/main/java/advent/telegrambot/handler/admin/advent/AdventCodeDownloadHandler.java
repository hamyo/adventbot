package advent.telegrambot.handler.admin.advent;

import advent.telegrambot.handler.MessageHandler;
import advent.telegrambot.handler.TelegramCommand;
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
import static advent.telegrambot.handler.TelegramCommand.ADVENTS_CODES_DOWNLOAD;

@Component
@RequiredArgsConstructor
public class AdventCodeDownloadHandler implements MessageHandler {
    private final AdventService adventService;
    private final PersonService personService;
    private final TelegramClient telegramClient;

    @SneakyThrows
    @Override
    public void handle(Update update) {
        Integer adventId = ADVENTS_CODES_DOWNLOAD.getIdFromAction(MessageUtils.getMessageText(update));

        SendDocument message = SendDocument.builder()
                .chatId(MessageUtils.getChatId(update))
                .document(new InputFile(
                        new ByteArrayInputStream(adventService.getCodes(adventId)),
                        "Коды_к_адвенту.txt"))
                .replyMarkup(MessageUtils.getAdminCodesActionKeyboard(adventId))
                .build();
        telegramClient.execute(message);
    }

    @Override
    public boolean canHandle(Update update) {
        return TelegramCommand.ADVENTS_CODES_DOWNLOAD.is(update) &&
                personService.isAdmin(MessageUtils.getTelegramUserId(update));
    }
}
