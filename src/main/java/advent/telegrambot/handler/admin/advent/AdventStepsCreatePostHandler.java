package advent.telegrambot.handler.admin.advent;

import advent.telegrambot.handler.MessageHandler;
import advent.telegrambot.handler.TelegramCommand;
import advent.telegrambot.repository.ClsQuestTypeRepository;
import advent.telegrambot.service.AdminProgressService;
import advent.telegrambot.service.AdventStepsCreateService;
import advent.telegrambot.service.PersonService;
import advent.telegrambot.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static advent.telegrambot.handler.TelegramCommand.ADVENTS_STEPS_CREATE;
import static advent.telegrambot.utils.MessageUtils.getTelegramUserId;

@Component
@RequiredArgsConstructor
public class AdventStepsCreatePostHandler implements MessageHandler {
    private final TelegramClient telegramClient;
    private final PersonService personService;
    private final AdminProgressService adminProgressService;
    private final ClsQuestTypeRepository clsQuestTypeRepository;
    private final AdventStepsCreateService adventStepsCreateService;

    @SneakyThrows
    @Override
    @Transactional
    public void handle(Update update) {
        long personId = getTelegramUserId(update);
        Pair<Integer, Integer> ids = adminProgressService.getAdventStepsCreateIds(personId);
        adventStepsCreateService.createStep(ids.getLeft(), ids.getRight(), update);

        SendMessage response = SendMessage.builder()
                .chatId(MessageUtils.getChatId(update))
                .text("""
                        Шаг успешно добавлен.
                        Теперь можно добавить контент (изображение, видео или музыку), просто отправив нужный файл.
                        Или можно создать новый шаг.
                        """)
                .replyMarkup(MessageUtils.getStepActionKeyboard(
                        ids.getLeft(),
                        clsQuestTypeRepository.findAll()
                ))
                .build();
        telegramClient.executeAsync(response);
    }

    @Override
    public boolean canHandle(Update update) {
        long personId = getTelegramUserId(update);
        return adminProgressService.isCurrentCommand(personId, ADVENTS_STEPS_CREATE) &&
                TelegramCommand.isNotAnyCommand(update) &&
                personService.isAdmin(personId);
    }
}
