package advent.telegrambot.handler.quest;

import advent.telegrambot.classifier.DataType;
import advent.telegrambot.classifier.QuestType;
import advent.telegrambot.domain.Step;
import advent.telegrambot.domain.quest.QuestWithAnyAnswer;
import advent.telegrambot.handler.StepCreateHandler;
import advent.telegrambot.repository.ClsQuestTypeRepository;
import advent.telegrambot.repository.StepRepository;
import advent.telegrambot.service.*;
import advent.telegrambot.utils.AppException;
import advent.telegrambot.utils.MessageUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collections;

import static advent.telegrambot.classifier.QuestType.ANY_ANSWER;
import static advent.telegrambot.utils.MessageUtils.getTelegramUserId;

@Component
@RequiredArgsConstructor
public class QuestWithAnyAnswerHandler implements QuestHandler<QuestWithAnyAnswer>, StepCreateHandler {
    private final StepService stepService;
    private final ClsDataTypeService clsDataTypeService;
    private final ClsQuestTypeService clsQuestTypeService;
    private final AdminProgressService adminProgressService;
    private final StepRepository stepRepository;
    private final StepCommon stepCommon;

    private final static int EXPECTED_ROWS = 5;
    private final ClsQuestTypeRepository clsQuestTypeRepository;

    @Override
    public void handle(@NotNull QuestWithAnyAnswer quest, Update update) {
        DataType answerType = DataType.of(update);
        if (answerType == null || quest.isNotNeedType(answerType)) {
            throw new AppException("Это не тот ответ, который ожидается\uD83D\uDE1F. Я жду " + quest.getRusNameNeedTypes());
        }

        stepService.handleNextSteps(
                quest.getStep().getAdvent(),
                quest.getStep().getDay(),
                quest.getStep().getOrder()
        );
    }

    @Override
    public Class<QuestWithAnyAnswer> getHandledQuestClass() {
        return QuestWithAnyAnswer.class;
    }

    private QuestType getQuestType() {
        return ANY_ANSWER;
    }

    @Override
    public boolean canHandle(Integer questType) {
        return getQuestType().is(questType);
    }

    @Override
    @Transactional(readOnly = true)
    public String getMessageForCreate() {
        String questType = clsQuestTypeService.getQuestTypeName(getQuestType().getId());
        String dataTypeDescription = clsDataTypeService.getAllDataTypeDescription();
        return "Для добавления шага (" + questType + ") введите:\n" +
                """
                        день,
                        порядок шага (оставьте строку пустой - порядок будет максимальный в рамках дня),
                        текст без переносов строки (если он нужен, если не нужен, то оставьте пустую строку),
                        подсказки на одной строчке, разделенные знаком | (если не нужны, то оставьте пустую строку),
                        идентификаторы возможных типов данных ответа, разделенные знаком |
                        """ +
                dataTypeDescription +
                """
                        .
                        Каждые новые данные вводятся с новой строки. Порядок важен.
                        Пример,
                        1
                        1
                        Привет, нарисуй новогоднюю открытку и пришли её фотографию
                        
                        1
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
        QuestWithAnyAnswer quest = new QuestWithAnyAnswer();
        quest.setStep(step);
        step.getQuests().add(quest);
        quest.setHints(stepCommon.parseHints(data[EXPECTED_ROWS - 2], quest));
        quest.setAllowedAnswerTypes(DataType.getIdsFromString(data[EXPECTED_ROWS - 1]));

        return step;
    }
}
