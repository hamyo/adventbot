package advent.telegrambot.handler.quest;

import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.domain.quest.QuestWithTextAnswer;
import advent.telegrambot.service.AdventService;
import advent.telegrambot.service.StepCommon;
import advent.telegrambot.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QuestWithTextAnswerHandler implements QuestHandler<QuestWithTextAnswer> {
    private final TelegramClient telegramClient;
    private final StepCommon stepCommon;
    private final AdventService adventService;

    private boolean isAnswerRight(List<String> rightValues, final Update update) {
        String userAnswer = MessageUtils.getMessageText(update);
        return rightValues.stream().anyMatch(rightValue -> rightValue.equalsIgnoreCase(userAnswer));
    }

    @SneakyThrows
    @Override
    public void handle(@NotNull QuestWithTextAnswer quest, Update update) {
        Advent advent = adventService.findByStepsQuestsId(quest.getId());

        if (isAnswerRight(quest.getRightValues(), update)) {
            SendMessage message = SendMessage.builder()
                    .chatId(advent.getChatId())
                    .text("Верно✅\uD83D\uDD25")
                    .build();
            telegramClient.execute(message);

            stepCommon.handleNextSteps(advent);
        } else {
            SendMessage message = SendMessage.builder()
                    .chatId(advent.getChatId())
                    .text("Ответ неверный❌")
                    .build();
            telegramClient.executeAsync(message);
        }
    }

    @Override
    public Class<QuestWithTextAnswer> getHandledQuestClass() {
        return QuestWithTextAnswer.class;
    }
}
