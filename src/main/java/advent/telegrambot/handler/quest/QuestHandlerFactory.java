package advent.telegrambot.handler.quest;

import advent.telegrambot.domain.Step;
import advent.telegrambot.domain.quest.Quest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuestHandlerFactory {
    private final List<QuestHandler> handlers;

    public void handle(Step currentStep, Update update) {
        if (currentStep.getQuests() == null || currentStep.getQuests().isEmpty()) {
            return;
        }

        Quest quest = currentStep.getQuests().getFirst();
        handlers.stream()
                .filter(handler -> handler.canHandle(quest))
                .findFirst()
                .ifPresentOrElse(
                        handler -> handler.handle(quest, update),
                        () -> log.warn("Handler is not found {}", currentStep));
    }
}
