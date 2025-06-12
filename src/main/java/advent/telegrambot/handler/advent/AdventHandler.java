package advent.telegrambot.handler.advent;

import advent.telegrambot.domain.advent.Advent;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface AdventHandler<E extends Advent> {
    default boolean canHandle(Advent advent) {
        return getHandledQuestClass().isInstance(advent);
    }

    void startDay(@NotNull E advent, @NonNull Short day, String messageText);
    void afterStepSave(@NotNull E advent);
    InlineKeyboardMarkup getAdminKeyboard(Integer adventId);

    Class<E> getHandledQuestClass();
}
