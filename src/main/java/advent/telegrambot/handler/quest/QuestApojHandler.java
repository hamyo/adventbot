package advent.telegrambot.handler.quest;

import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.domain.dto.BullsAndCowsResult;
import advent.telegrambot.domain.quest.QuestApoj;
import advent.telegrambot.service.AdventService;
import advent.telegrambot.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class QuestApojHandler implements QuestHandler<QuestApoj> {
    private final AdventService adventService;

    @Override
    public void handle(@NotNull QuestApoj quest, Update update) {
        Advent advent = adventService.findByStepsQuestsId(quest.getId());

        //BullsAndCowsResult result = questBullsAndCowsService.checkAnswer(advent.getId(), MessageUtils.getMessageText(update));
    }

    @Override
    public Class<QuestApoj> getHandledQuestClass() {
        return QuestApoj.class;
    }
}
