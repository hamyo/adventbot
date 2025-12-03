package advent.telegrambot.handler.quest;

import advent.telegrambot.classifier.DataType;
import advent.telegrambot.classifier.QuestType;
import advent.telegrambot.domain.Step;
import advent.telegrambot.domain.quest.QuestApoj;
import advent.telegrambot.domain.quest.QuestWithAnyAnswer;
import advent.telegrambot.handler.StepCreateHandler;
import advent.telegrambot.service.AdminProgressService;
import advent.telegrambot.service.ClsQuestTypeService;
import advent.telegrambot.service.StepCommon;
import advent.telegrambot.service.StepService;
import advent.telegrambot.utils.AppException;
import advent.telegrambot.utils.MessageUtils;
import advent.telegrambot.utils.NumberUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;

import static advent.telegrambot.classifier.QuestType.APOJ;
import static advent.telegrambot.utils.MessageUtils.getTelegramUserId;

@Service
@RequiredArgsConstructor
public class QuestApojService implements StepCreateHandler {
    private final ClsQuestTypeService clsQuestTypeService;
    private final StepService stepService;
    private final AdminProgressService adminProgressService;
    private final StepCommon stepCommon;

    private final static int EXPECTED_ROWS = 6;

    private QuestType getQuestType() {
        return APOJ;
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
                        текст без переносов строки (если не нужен, то оставьте пустую строку).
                        подсказки на одной строчке, разделенные знаком | (если не нужны, то оставьте пустую строку),
                        сколько секунд брать из песни (если нужно весь файл, то оставьте пустую строку),
                        на сколько частей разбить песню (если разбиение не нужно, то укажите 1).
                        Каждые новые данные вводятся с новой строки. Порядок важен.
                        Пример,
                        1
                        1
                        Привет, это игра АПОЖ. В ответ на каждую часть песни наоборот, нужно прислать записанный вами повтор. Его лучше записать отдельно, а не в telegram (чтобы была возможность перезаписи).
                        детская песня|песня про зайчиков
                        60
                        3
                        """;
    }

    @Override
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
        QuestApoj quest = new QuestApoj();
        quest.setStep(step);
        step.getQuests().add(quest);
        if (StringUtils.isNotBlank(data[EXPECTED_ROWS - 2])) {
            quest.setSecondDuration(
                    NumberUtils.parseShort(data[EXPECTED_ROWS - 2],
                            "сколько секунд брать из песни"));
        }
        quest.setPartCount(NumberUtils.parseShort(data[EXPECTED_ROWS - 1], "на сколько частей разбить песню"));
        quest.getHints().addAll(stepCommon.parseHints(data[EXPECTED_ROWS - 3], quest));

        return step;
    }
}
