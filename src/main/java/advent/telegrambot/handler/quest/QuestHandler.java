package advent.telegrambot.handler.quest;

import advent.telegrambot.domain.quest.Quest;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface QuestHandler<E extends Quest> {
    void handle(@NotNull E quest, Update update);
    default boolean canHandle(Quest quest) {
        return getHandledQuestClass().isInstance(quest);
    }

    Class<E> getHandledQuestClass();
}
