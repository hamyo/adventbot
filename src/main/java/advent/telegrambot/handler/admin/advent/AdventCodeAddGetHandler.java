package advent.telegrambot.handler.admin.advent;

import advent.telegrambot.handler.MessageHandler;
import advent.telegrambot.service.AdminProgressService;
import advent.telegrambot.service.PersonService;
import advent.telegrambot.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static advent.telegrambot.handler.TelegramCommand.ADVENTS_CODES_ADD;

@Component
@RequiredArgsConstructor
public class AdventCodeAddGetHandler implements MessageHandler {
    private final PersonService personService;
    private final TelegramClient telegramClient;
    private final AdminProgressService adminProgressService;

    @SneakyThrows
    @Override
    public void handle(Update update) {
        Integer adventId = ADVENTS_CODES_ADD.getIdFromAction(MessageUtils.getMessageText(update));

        SendMessage message = SendMessage.builder()
                .chatId(MessageUtils.getChatId(update))
                .text("""
                        Для создания кодов введите нужное количество дней.
                        Если по адвенту уже есть коды, то лишние будут удалены, а недостающие добавлены.
                        """)
                .replyMarkup(MessageUtils.getAdminCodesActionKeyboard(adventId))
                .build();
        telegramClient.execute(message);
        adminProgressService.saveCodeAdventId(MessageUtils.getTelegramUserId(update), adventId);
    }

    @Override
    public boolean canHandle(Update update) {
        return ADVENTS_CODES_ADD.is(update) &&
                personService.isAdmin(MessageUtils.getTelegramUserId(update));
    }
}
