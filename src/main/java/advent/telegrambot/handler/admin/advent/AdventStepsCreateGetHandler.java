package advent.telegrambot.handler.admin.advent;

import advent.telegrambot.handler.MessageHandler;
import advent.telegrambot.handler.StepCreateHandlerFactory;
import advent.telegrambot.handler.TelegramCommand;
import advent.telegrambot.handler.advent.AdventHandlerFactory;
import advent.telegrambot.service.AdminProgressService;
import advent.telegrambot.service.AdventService;
import advent.telegrambot.service.PersonService;
import advent.telegrambot.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@RequiredArgsConstructor
public class AdventStepsCreateGetHandler implements MessageHandler {
    private final TelegramClient telegramClient;
    private final PersonService personService;
    private final StepCreateHandlerFactory stepCreateHandlerFactory;
    private final AdminProgressService adminProgressService;
    private final AdventHandlerFactory adventHandlerFactory;
    private final AdventService adventService;

    @SneakyThrows
    @Override
    public void handle(Update update) {
        Pair<Integer, Integer> ids = TelegramCommand.getIdsFromStepCreateCommand(MessageUtils.getMessageText(update));
        String messageText = stepCreateHandlerFactory.getMessageForCreate(ids.getRight());
        adminProgressService.saveAdventStepsCreate(
                MessageUtils.getTelegramUserId(update),
                MessageUtils.getMessageText(update));

        SendMessage response = SendMessage.builder()
                .chatId(MessageUtils.getChatId(update))
                .text(messageText)
                .replyMarkup(
                        adventHandlerFactory.getAdminKeyboard(
                                adventService.findById(ids.getLeft())))
                .build();
        telegramClient.execute(response);
    }

    @Override
    public boolean canHandle(Update update) {
        return TelegramCommand.ADVENTS_STEPS_CREATE.is(update) &&
                personService.isAdmin(MessageUtils.getTelegramUserId(update));
    }
}
