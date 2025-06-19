package advent.telegrambot.handler.quest;

import advent.telegrambot.classifier.QuestType;
import advent.telegrambot.domain.AdventCurrentStep;
import advent.telegrambot.domain.Step;
import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.domain.dto.BullsAndCowsResult;
import advent.telegrambot.domain.quest.QuestBullsAndCows;
import advent.telegrambot.handler.StepCreateHandler;
import advent.telegrambot.repository.StepRepository;
import advent.telegrambot.service.*;
import advent.telegrambot.utils.AppException;
import advent.telegrambot.utils.MessageUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.*;

import static advent.telegrambot.classifier.QuestType.BULLS_AND_COWS;
import static advent.telegrambot.utils.MessageUtils.getTelegramUserId;

@Component
@RequiredArgsConstructor
public class QuestBullsAndCowsHandler implements QuestHandler<QuestBullsAndCows> {
    private final AdventCurrentStepService adventCurrentStepService;
    private final TelegramClient telegramClient;
    private final StepCommon stepCommon;
    private final AdventService adventService;
    private final QuestBullsAndCowsService questBullsAndCowsService;

    @SneakyThrows
    @Override
    public void handle(@NotNull QuestBullsAndCows quest, Update update) {
        Advent advent = adventService.findByStepsQuestsId(quest.getId());

        BullsAndCowsResult result = questBullsAndCowsService.checkAnswer(advent.getId(), MessageUtils.getMessageText(update));
        SendMessage message = SendMessage.builder()
                .chatId(advent.getChatId())
                .text(String.format("Попытка %s: быков \uD83D\uDC02 %s, коров \uD83D\uDC04 %s",
                        result.attemptCount(),
                        result.bullsCount(),
                        result.cowsCount()))
                .build();
        telegramClient.execute(message);

        if (result.isFinished()) {
            message = SendMessage.builder()
                    .chatId(advent.getChatId())
                    .text("Задача решена! ✔\uFE0F")
                    .build();
            telegramClient.execute(message);

            AdventCurrentStep adventCurrentStep = adventCurrentStepService.findById(advent.getId());
            stepCommon.handleNextSteps(
                    advent,
                    adventCurrentStep.getStep().getDay(),
                    adventCurrentStep.getStep().getOrder()
            );
        }
    }

    @Override
    public Class<QuestBullsAndCows> getHandledQuestClass() {
        return QuestBullsAndCows.class;
    }
}
