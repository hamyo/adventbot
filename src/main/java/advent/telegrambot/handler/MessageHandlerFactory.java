package advent.telegrambot.handler;

import advent.telegrambot.utils.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MessageHandlerFactory {
    private final List<MessageHandler> handlers;

    public void handle(Update update) {
        handlers.stream()
                .filter(handler -> handler.canHandle(update))
                .min(Comparator.comparing(MessageHandler::getPriority))
                .ifPresent(handler -> handler.handle(update));
    }
}

