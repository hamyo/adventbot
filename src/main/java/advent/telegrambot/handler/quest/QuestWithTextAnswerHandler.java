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

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class QuestWithTextAnswerHandler implements QuestHandler<QuestWithTextAnswer> {
    private final TelegramClient telegramClient;
    private final StepCommon stepCommon;
    private final AdventService adventService;

    private static final Pattern WORD_SPLIT_PATTERN = Pattern.compile("[ ,.:?!;()\\[\\]/\\\\-]+");

    private Pattern getWordSplitPattern() {
        return WORD_SPLIT_PATTERN;
    }

    private boolean isAnswerRight(List<String> rightValues, Update update) {
        String userAnswer = MessageUtils.getMessageText(update);
        return isAnswerRight(rightValues, userAnswer);
    }

    protected boolean isAnswerRight(List<String> rightValues, String userAnswer) {
        String[] normalizeUserAnswer = normalize(userAnswer);
        return rightValues.stream()
                .anyMatch(rightValue -> Arrays.equals(normalize(rightValue), normalizeUserAnswer));
    }

    private String[] normalize(String value) {
        return Arrays.stream(
                getWordSplitPattern()
                        .split(value))
                .filter(word -> !word.isBlank())
                .map(String::toLowerCase)
                .toArray(String[]::new);
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
            telegramClient.execute(message);
        }
    }

    @Override
    public Class<QuestWithTextAnswer> getHandledQuestClass() {
        return QuestWithTextAnswer.class;
    }
}
