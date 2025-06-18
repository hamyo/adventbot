package advent.telegrambot.handler;

import advent.telegrambot.domain.AdventCurrentStep;
import advent.telegrambot.domain.Step;
import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.handler.advent.AdventHandlerFactory;
import advent.telegrambot.handler.quest.QuestHandlerFactory;
import advent.telegrambot.repository.AdventCurrentStepRepository;
import advent.telegrambot.repository.StepRepository;
import advent.telegrambot.service.AdventService;
import advent.telegrambot.service.PersonService;
import advent.telegrambot.service.StepCommon;
import advent.telegrambot.service.StepService;
import advent.telegrambot.utils.AppException;
import advent.telegrambot.utils.DateUtils;
import advent.telegrambot.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static advent.telegrambot.utils.MessageUtils.getTelegramUserId;

@Component
@RequiredArgsConstructor
public class PersonMessageHandler implements MessageHandler {
    private final PersonService personService;
    private final AdventService adventService;
    private final AdventCurrentStepRepository adventCurrentStepRepository;
    private final StepRepository stepRepository;
    private final TelegramClient telegramClient;
    private final QuestHandlerFactory questHandlerFactory;
    private final AdventHandlerFactory adventHandlerFactory;
    private final StepCommon stepCommon;

    @Override
    public int getPriority() {
        return 100;
    }

    private short getAdventDay(Advent advent) {
        long stepDay = ChronoUnit.DAYS.between(
                advent.getStartDate(),
                LocalDate.now()
        ) + 1;

        if (stepDay <= 0) {
            throw new AppException("Ещё рано. Адвент начнётся " +
                    advent.getStartDate().format(DateUtils.getRusDateFormatter()));
        }

        if (stepDay > Short.MAX_VALUE) {
            throw new AppException("Адвент уже закончился - прошло много времени.");
        }

        return (short) stepDay;
    }

    @SneakyThrows
    @Override
    public void handle(Update update) {
        long chatId = MessageUtils.getChatId(update);
        Advent advent = adventService.findByChatId(chatId);
        short stepDay = getAdventDay(advent);

        Optional<AdventCurrentStep> savedCurrentStep = adventCurrentStepRepository.findById(advent.getId());
        Step currentStep = savedCurrentStep.map(AdventCurrentStep::getStep).orElse(null);
        if (currentStep == null || currentStep.getDay() != stepDay) {
            // Стартуем новый день
            startDay(advent, stepDay, MessageUtils.getMessageText(update));
        } else {
            // Текущий шаг без задачи
            if (currentStep.getQuests().isEmpty()) {
                Optional<Step> nextStep = stepRepository.findFirstByAdventAndDayAndOrderGreaterThanOrderByOrderAsc(
                        advent,
                        currentStep.getDay(),
                        currentStep.getOrder());
                if (nextStep.isEmpty()) {
                    telegramClient.executeAsync(
                            SendMessage.builder()
                                    .chatId(chatId)
                                    .text("Задания на сегодня закончены\uD83D\uDE0B.")
                                    .build());
                } else {
                    stepCommon.handleNextSteps(advent, currentStep.getDay(), currentStep.getOrder());
                }
            } else {
                questHandlerFactory.handle(currentStep, update);
            }
        }
    }

    private void startDay(Advent advent, short day, String messageText) {
        adventHandlerFactory.startDay(advent, day, messageText);
    }


    @Override
    public boolean canHandle(Update update) {
        return personService.isExist(getTelegramUserId(update)) &&
                TelegramCommand.isNotAnyCommand(update);
    }
}
