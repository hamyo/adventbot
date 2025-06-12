package advent.telegrambot.handler;

import advent.telegrambot.domain.AdventCurrentStep;
import advent.telegrambot.domain.Step;
import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.domain.advent.AdventByCode;
import advent.telegrambot.handler.advent.AdventHandlerFactory;
import advent.telegrambot.handler.quest.QuestHandlerFactory;
import advent.telegrambot.repository.AdminProgressRepository;
import advent.telegrambot.repository.AdventCurrentStepRepository;
import advent.telegrambot.repository.StepRepository;
import advent.telegrambot.service.AdventService;
import advent.telegrambot.service.PersonService;
import advent.telegrambot.service.StepService;
import advent.telegrambot.utils.AppException;
import advent.telegrambot.utils.DateUtils;
import advent.telegrambot.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static advent.telegrambot.utils.MessageUtils.getTelegramUserId;

@Service
@RequiredArgsConstructor
public class StepHandler implements MessageHandler {
    private final PersonService personService;
    private final AdventService adventService;
    private final AdventCurrentStepRepository adventCurrentStepRepository;
    private final StepRepository stepRepository;
    private final StepService stepService;
    private final TelegramClient telegramClient;
    private final QuestHandlerFactory questHandlerFactory;
    private final AdminProgressRepository adminProgressRepository;
    private final AdventHandlerFactory adventHandlerFactory;

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
    @Transactional
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
                    stepService.handleNextSteps(advent, currentStep.getDay(), currentStep.getOrder());
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
    @Transactional(readOnly = true)
    public boolean canHandle(Update update) {
        return personService.isExist(getTelegramUserId(update)) &&
                !adminProgressRepository.existsById(getTelegramUserId(update)) &&
                TelegramCommand.isNotAnyCommand(update);
    }
}
