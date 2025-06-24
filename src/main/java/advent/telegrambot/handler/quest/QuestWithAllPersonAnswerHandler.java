package advent.telegrambot.handler.quest;

import advent.telegrambot.classifier.DataType;
import advent.telegrambot.domain.Person;
import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.domain.quest.QuestWithAllPersonAnswer;
import advent.telegrambot.service.AdventService;
import advent.telegrambot.service.StepCommon;
import advent.telegrambot.utils.AppException;
import advent.telegrambot.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;


@Component
@RequiredArgsConstructor
public class QuestWithAllPersonAnswerHandler implements QuestHandler<QuestWithAllPersonAnswer> {
    private final TelegramClient telegramClient;
    private final AdventService adventService;
    private final StepCommon stepCommon;
    private final QuestWithAllPersonAnswerService questWithAllPersonAnswerService;

    @SneakyThrows
    @Override
    public void handle(@NotNull QuestWithAllPersonAnswer quest, Update update) {
        DataType answerType = DataType.of(update);
        if (answerType == null || quest.isNotNeedType(answerType)) {
            throw new AppException("Это не тот ответ, который ожидается\uD83D\uDE1F. Я жду " + quest.getRusNameNeedTypes());
        }

        Long answeredPersonId = MessageUtils.getTelegramUserId(update);
        Advent advent = adventService.findByStepsQuestsId(quest.getId());
        Pair<Person, Boolean> checkResult = questWithAllPersonAnswerService.checkAnswer(advent.getId(), answeredPersonId);

        if (checkResult.getLeft() != null) {
            SendMessage message = SendMessage.builder()
                    .chatId(advent.getChatId())
                    .text(MessageUtils.getResponseTextForUser(checkResult.getLeft().getNameNominative()))
                    .build();
            telegramClient.execute(message);
        }

        if (checkResult.getRight()) {
            stepCommon.handleNextSteps(advent);
        }
    }

    @Override
    public Class<QuestWithAllPersonAnswer> getHandledQuestClass() {
        return QuestWithAllPersonAnswer.class;
    }
}
