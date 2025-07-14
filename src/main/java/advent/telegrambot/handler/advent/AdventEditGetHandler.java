package advent.telegrambot.handler.advent;

import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.handler.MessageHandler;
import advent.telegrambot.handler.TelegramCommand;
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
public class AdventEditGetHandler implements MessageHandler {
    private final PersonService personService;
    private final AdventHandlerFactory adventHandlerFactory;
    private final TelegramClient telegramClient;
    private final AdventService adventService;

    @SneakyThrows
    @Override
    public void handle(Update update) {
        Integer adventId = TelegramCommand.ADVENTS_EDIT.getIdFromAction(MessageUtils.getMessageText(update));
        Advent advent = adventService.findById(adventId);
        telegramClient.execute(
                SendMessage.builder()
                        .text(advent.getInfo())
                        .chatId(MessageUtils.getChatId(update))
                        .build()
        );
        telegramClient.execute(
                SendMessage.builder()
                        .text("Для изменения данных, выберите нужный пункт из меню")
                        .chatId(MessageUtils.getChatId(update))
                        .replyMarkup(adventHandlerFactory.getAdminKeyboard(advent))
                        .build()
        );
    }

    @Override
    public boolean canHandle(Update update) {
        return TelegramCommand.ADVENTS_EDIT.is(update) &&
                personService.isAdmin(MessageUtils.getTelegramUserId(update));
    }
}
