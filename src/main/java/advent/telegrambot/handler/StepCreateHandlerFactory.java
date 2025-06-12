package advent.telegrambot.handler;

import advent.telegrambot.utils.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StepCreateHandlerFactory {
    private final List<StepCreateHandler> handlers;

    public String getMessageForCreate(Integer questType) {
        return handlers.stream()
                .filter(handler -> handler.canHandle(questType))
                .map(StepCreateHandler::getMessageForCreate)
                .findFirst()
                .orElseThrow(() -> new AppException("Для типа задания (" +
                        (questType == null ? "" : questType) +
                        ") не найден обработчик"));
    }

    public Long createStep(Integer questType, Update update) {
        return handlers.stream()
                .filter(handler -> handler.canHandle(questType))
                .map(handler -> handler.createStep(update))
                .findFirst()
                .orElseThrow(() -> new AppException("Для типа задания (" +
                                    (questType == null ? "" : questType) +
                                    ") не найден обработчик"));
    }
}

