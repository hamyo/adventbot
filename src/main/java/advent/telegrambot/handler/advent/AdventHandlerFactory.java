package advent.telegrambot.handler.advent;

import advent.telegrambot.domain.advent.Advent;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdventHandlerFactory {
    private final List<AdventHandler> adventHandlers;

    public void startDay(Advent advent, @NonNull Short day, String messageText) {
        adventHandlers.stream().
                filter(hadler -> hadler.canHandle(advent))
                .findFirst()
                .ifPresent(hadler -> hadler.startDay(advent, day, messageText));
    }

    public InlineKeyboardMarkup getAdminKeyboard(@NonNull Advent advent) {
        return adventHandlers.stream().
                filter(hadler -> hadler.canHandle(advent))
                .findFirst()
                .map(hadler -> hadler.getAdminKeyboard(advent.getId()))
                .orElse(null);
    }

    public void afterStepSave(Advent advent) {
        adventHandlers.stream().
                filter(hadler -> hadler.canHandle(advent))
                .findFirst()
                .ifPresent(hadler -> hadler.afterStepSave(advent));
    }

}
