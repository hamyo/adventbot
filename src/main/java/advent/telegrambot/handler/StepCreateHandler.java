package advent.telegrambot.handler;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface StepCreateHandler {
    boolean canHandle(Integer questType);
    String getMessageForCreate();
    Long createStep(Update update);
}
