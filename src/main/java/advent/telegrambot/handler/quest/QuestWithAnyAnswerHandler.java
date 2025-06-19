package advent.telegrambot.handler.quest;

import advent.telegrambot.classifier.DataType;
import advent.telegrambot.domain.Person;
import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.domain.quest.QuestWithAnyAnswer;
import advent.telegrambot.service.AdventCurrentStepService;
import advent.telegrambot.service.AdventService;
import advent.telegrambot.service.StepCommon;
import advent.telegrambot.utils.AppException;
import advent.telegrambot.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class QuestWithAnyAnswerHandler implements QuestHandler<QuestWithAnyAnswer> {
    private final StepCommon stepCommon;
    private final AdventService adventService;

    @Override
    public void handle(@NotNull QuestWithAnyAnswer quest, Update update) {
        DataType answerType = DataType.of(update);
        if (answerType == null || quest.isNotNeedType(answerType)) {
            throw new AppException("Это не тот ответ, который ожидается\uD83D\uDE1F. Я жду " + quest.getRusNameNeedTypes());
        }

        Advent advent = adventService.findByStepsQuestsId(quest.getId());
        stepCommon.handleNextSteps(advent);
    }

    @Override
    public Class<QuestWithAnyAnswer> getHandledQuestClass() {
        return QuestWithAnyAnswer.class;
    }
}
