package advent.telegrambot.handler.admin.advent;

import advent.telegrambot.domain.ClsQuestType;
import advent.telegrambot.handler.MessageHandler;
import advent.telegrambot.handler.TelegramCommand;
import advent.telegrambot.repository.ClsQuestTypeRepository;
import advent.telegrambot.service.PersonService;
import advent.telegrambot.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.stream.Collectors;

import static advent.telegrambot.utils.MessageUtils.getTelegramUserId;

@Component
@RequiredArgsConstructor
public class AdventStepsGetHandler implements MessageHandler {
    private final TelegramClient telegramClient;
    private final PersonService personService;
    private final ClsQuestTypeRepository clsQuestTypeRepository;


    @SneakyThrows
    @Override
    public void handle(Update update) {
        List<ClsQuestType> questTypes = clsQuestTypeRepository.findAll();
        String messageText = "Для создания шага выберите тип задания или вариант без задания\n" +
                questTypes.stream()
                        .map(questType -> questType.getId() + " - " + questType.getDescription())
                        .collect(Collectors.joining("\n"));
        Integer adventId = TelegramCommand.ADVENTS_STEPS.getIdFromAction(MessageUtils.getMessageText(update));
        SendMessage response = SendMessage.builder()
                .chatId(MessageUtils.getChatId(update))
                .text(messageText)
                .replyMarkup(MessageUtils.getStepActionKeyboard(adventId, questTypes))
                .build();
        telegramClient.executeAsync(response);
    }

    @Override
    public boolean canHandle(Update update) {
        return TelegramCommand.ADVENTS_STEPS.is(update) &&
                personService.isAdmin(MessageUtils.getTelegramUserId(update));
    }
}
