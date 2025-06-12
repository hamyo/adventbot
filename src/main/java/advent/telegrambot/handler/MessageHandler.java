package advent.telegrambot.handler;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface MessageHandler {
    void handle(Update update);
    boolean canHandle(Update update);
    default int getPriority() {
        return 1;
    }
}
