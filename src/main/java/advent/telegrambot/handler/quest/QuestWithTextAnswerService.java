package advent.telegrambot.handler.quest;

import advent.telegrambot.classifier.QuestType;
import advent.telegrambot.domain.Step;
import advent.telegrambot.domain.quest.QuestWithTextAnswer;
import advent.telegrambot.handler.StepCreateHandler;
import advent.telegrambot.repository.StepRepository;
import advent.telegrambot.service.AdminProgressService;
import advent.telegrambot.service.ClsQuestTypeService;
import advent.telegrambot.service.StepCommon;
import advent.telegrambot.service.StepService;
import advent.telegrambot.utils.AppException;
import advent.telegrambot.utils.MessageUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;

import static advent.telegrambot.classifier.QuestType.TEXT_ANSWER;
import static advent.telegrambot.utils.MessageUtils.getTelegramUserId;

@Service
@RequiredArgsConstructor
public class QuestWithTextAnswerService implements StepCreateHandler {
    private final StepCommon stepCommon;
    private final AdminProgressService adminProgressService;
    private final StepService stepService;
    private final ClsQuestTypeService clsQuestTypeService;

    private final int EXPECTED_ROWS = 5;

    private QuestType getQuestType() {
        return TEXT_ANSWER;
    }

    @Override
    public boolean canHandle(Integer questType) {
        return getQuestType().is(questType);
    }

    @Override
    @Transactional(readOnly = true)
    public String getMessageForCreate() {
        String questType = clsQuestTypeService.getQuestTypeName(getQuestType().getId());
        return "Для добавления шага (" + questType + ") введите:\n" +
                """
                        день,
                        порядок шага (оставьте строку пустой - порядок будет максимальный в рамках дня),
                        текст без переносов строки (если не нужен, то оставьте пустую строку),
                        подсказки на одной строчке, разделенные знаком | (если не нужны, то оставьте пустую строку),
                        возможные правильные варианты ответа на одной строчке, разделенные знаком |.
                        Каждые новые данные вводятся с новой строки. Порядок важен.
                        Пример,
                        1
                        1
                        Привет, угадай новогодний фильм по звуковому фрагменту.
                        Главный герой фильма зелёный, но не ёлка|Главный герой ненавидит бритву
                        Гринч – похититель Рождества|Гринч
                        """;
    }

    @Override
    @Transactional
    public Long createStep(Update update) {
        long personId = getTelegramUserId(update);
        Pair<Integer, Integer> ids = adminProgressService.getAdventStepsCreateIds(personId);
        Step step = createStep(MessageUtils.getMessageText(update), ids.getLeft());
        stepService.save(step);
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
        QuestWithTextAnswer quest = new QuestWithTextAnswer();
        quest.setStep(step);
        step.getQuests().add(quest);
        quest.getHints().addAll(stepCommon.parseHints(data[EXPECTED_ROWS - 2], quest));
        quest.setRightValues(
                Arrays.stream(data[EXPECTED_ROWS - 1].split("\\|"))
                        .filter(StringUtils::isNotBlank)
                        .toList());

        return step;
    }
}
