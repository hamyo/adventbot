package advent.telegrambot.service;

import advent.telegrambot.handler.StepCreateHandlerFactory;
import advent.telegrambot.handler.advent.AdventHandlerFactory;
import advent.telegrambot.utils.MessageUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;

import static advent.telegrambot.handler.TelegramCommand.ADVENTS_STEPS_CREATED;

@Service
@RequiredArgsConstructor
public class AdventStepsCreateService {
    private final StepCreateHandlerFactory stepCreateHandlerFactory;
    private final AdventHandlerFactory adventHandlerFactory;
    private final AdminProgressService adminProgressService;
    private final AdventService adventService;

    @Transactional
    public void createStep(@NonNull Integer adventId, Integer questType, Update update) {
        Long stepId = stepCreateHandlerFactory.createStep(questType, update);
        adventHandlerFactory.afterStepSave(adventService.findById(adventId));
        adminProgressService.save(
                MessageUtils.getTelegramUserId(update),
                ADVENTS_STEPS_CREATED,
                stepId);
    }
}
