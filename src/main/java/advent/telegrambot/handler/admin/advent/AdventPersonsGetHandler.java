package advent.telegrambot.handler.admin.advent;

import advent.telegrambot.handler.MessageHandler;
import advent.telegrambot.handler.TelegramCommand;
import advent.telegrambot.handler.advent.AdventHandlerFactory;
import advent.telegrambot.service.AdminProgressService;
import advent.telegrambot.service.AdventService;
import advent.telegrambot.service.PersonService;
import advent.telegrambot.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static advent.telegrambot.handler.TelegramCommand.ADVENTS_PERSONS;
import static advent.telegrambot.utils.MessageUtils.getTelegramUserId;

@Component
@RequiredArgsConstructor
public class AdventPersonsGetHandler implements MessageHandler {
    private final PersonService personService;
    private final AdminProgressService adminProgressService;
    private final TelegramClient telegramClient;
    private final AdventHandlerFactory adventHandlerFactory;
    private final AdventService adventService;

    @SneakyThrows
    @Override
    public void handle(Update update) {
        Integer adventId = ADVENTS_PERSONS.getIdFromAction(MessageUtils.getMessageText(update));
        adminProgressService.saveAdventId(getTelegramUserId(update), adventId);

        SendMessage response = SendMessage.builder()
                .chatId(MessageUtils.getChatId(update))
                .text("""
                        Для обновления участников адвента, введите его id из телеграмма и имя в именительном падеже.
                        Каждого нового участника укажите на новой строчке. Укажите всех участников - неуказанные (если они связаны с адвентом) будут удалены из адвента.
                        Пример,
                        124578 Костя
                        1261444 Маша
                        """)
                .replyMarkup(
                        adventHandlerFactory.getAdminKeyboard(
                                adventService.findById(adventId)))
                .build();
        telegramClient.executeAsync(response);
    }

    @Override
    public boolean canHandle(Update update) {
        return TelegramCommand.ADVENTS_PERSONS.is(update) &&
                personService.isAdmin(MessageUtils.getTelegramUserId(update));
    }
}
