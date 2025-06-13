package advent.telegrambot.handler.quest;

import advent.telegrambot.domain.Person;
import advent.telegrambot.domain.Step;
import advent.telegrambot.domain.quest.QuestWithAdminDecision;
import advent.telegrambot.handler.StepCreateHandler;
import advent.telegrambot.repository.ClsQuestTypeRepository;
import advent.telegrambot.repository.PersonRepository;
import advent.telegrambot.repository.StepRepository;
import advent.telegrambot.service.*;
import advent.telegrambot.utils.AppException;
import advent.telegrambot.utils.MessageUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Collections;

import static advent.telegrambot.classifier.QuestType.ADMIN_DECISION;
import static advent.telegrambot.utils.MessageUtils.getTelegramUserId;


@Service
@RequiredArgsConstructor
public class QuestWithAdminDecisionHandler implements QuestHandler<QuestWithAdminDecision>, StepCreateHandler {
    private final StepService stepService;
    private final StepCommon stepCommon;
    private final TelegramClient telegramClient;
    private final PersonRepository personRepository;
    private final StepRepository stepRepository;
    private final AdminProgressService adminProgressService;
    private final ClsQuestTypeRepository clsQuestTypeRepository;
    private final AdventService adventService;

    private final static int EXPECTED_ROWS = 4;

    @Override
    @Transactional
    public void handle(@NotNull QuestWithAdminDecision quest, Update update) {
        long userId = getTelegramUserId(update);
        personRepository.findById(userId)
                .map(Person::getIsAdmin)
                .ifPresentOrElse(person -> {
                    Step step = quest.getStep();
                    stepService.handleNextSteps(
                            step.getAdvent(),
                            step.getDay(),
                            step.getOrder());
                }, () -> {
                    try {
                        telegramClient.executeAsync(
                                SendMessage
                                        .builder()
                                        .chatId(quest.getStep().getAdvent().getChatId())
                                        .text("Нужно одобрение администратора")
                                        .build()
                        );
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public Class<QuestWithAdminDecision> getHandledQuestClass() {
        return QuestWithAdminDecision.class;
    }

    @Override
    public boolean canHandle(Integer questType) {
        return ADMIN_DECISION.is(questType);
    }

    @Override
    public String getMessageForCreate() {
        return """
                Для добавления создания шага введите день, порядок шага (оставьте строку пустой - порядок будет максимальный в рамках дня), текст без переносов строки (если не нужен, то оставьте пустую строку), подсказки на одной строчке, разделенные знаком | (если не нужны, то оставьте пустую строку).
                Каждые новые данные вводятся с новой строки. Порядок важен.
                Пример,
                1
                1
                Привет, посмотри какой-нибудь новогодний фильм. Администратор напишет о выполнении.
                
                """;
    }

    @Override
    @Transactional
    public Long createStep(Update update) {
        long personId = getTelegramUserId(update);
        Pair<Integer, Integer> ids = adminProgressService.getAdventStepsCreateIds(personId);
        Step step = createStep(MessageUtils.getMessageText(update), ids.getLeft());
        stepRepository.save(step);
        return step.getId();
    }

    private Step createStep(String input, @NonNull Integer adventId) {
        if (input == null) {
            throw new AppException("Нет данных для создания шага");
        }

        String[] data = input.split("\n");
        if (data.length != EXPECTED_ROWS) {
            throw new AppException("Ожидаются данные на " + EXPECTED_ROWS + " строчках");
        }

        Step step = stepCommon.createStep(data, adventId);
        QuestWithAdminDecision quest = new QuestWithAdminDecision();
        quest.setStep(step);
        step.setQuests(Collections.singletonList(quest));
        quest.setHints(stepCommon.parseHints(data[EXPECTED_ROWS - 1], quest));

        return step;
    }
}
