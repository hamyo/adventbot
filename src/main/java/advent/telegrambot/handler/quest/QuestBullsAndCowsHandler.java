package advent.telegrambot.handler.quest;

import advent.telegrambot.classifier.QuestType;
import advent.telegrambot.domain.AdventCurrentStep;
import advent.telegrambot.domain.Step;
import advent.telegrambot.domain.advent.Advent;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.*;

import static advent.telegrambot.classifier.QuestType.BULLS_AND_COWS;
import static advent.telegrambot.utils.MessageUtils.getTelegramUserId;

@Service
@RequiredArgsConstructor
public class QuestBullsAndCowsHandler implements QuestHandler<QuestBullsAndCows>, StepCreateHandler {
    private final AdventCurrentStepService adventCurrentStepService;
    private final StepService stepService;
    private final TelegramClient telegramClient;
    private final StepRepository stepRepository;
    private final AdminProgressService adminProgressService;
    private final StepCommon stepCommon;
    private final ClsQuestTypeService clsQuestTypeService;

    private final static String GUESS_WORD = "GUESS_WORD";
    private final static String NUMBER_OF_ATTEMPTS = "NUMBER_OF_ATTEMPTS";
    private final static int EXPECTED_ROWS = 3;


    private int getNumberOfAttempts(Map<String, Object> data) {
        return (int) data.getOrDefault(NUMBER_OF_ATTEMPTS, 0);
    }

    private void incrementNumberOfAttempts(Map<String, Object> data) {
        int attempt = getNumberOfAttempts(data);
        data.put(NUMBER_OF_ATTEMPTS, attempt + 1);
    }

    private char[] getGuessWord(Map<String, Object> data) {
        char[] word = (char[]) data.get(GUESS_WORD);
        if (word == null) {
            word = generateNumber();
            saveGuessWord(data, word);
        }

        return word;
    }

    private void saveGuessWord(Map<String, Object> data, char[] guessWord) {
        data.put(GUESS_WORD, guessWord);
    }

    private char[] generateNumber() {
        SplittableRandom random = new SplittableRandom();
        int number = random.nextInt(123, 9876);
        char[] digits = StringUtils.leftPad(String.valueOf(number), 4, "0").toCharArray();

        Set<Character> usedDigits = new HashSet<>(4);
        Map<Integer, Character> repeatDigits = new HashMap<>(3);
        for (int i = 0; i < digits.length; i++) {
            if (usedDigits.contains(digits[i])) {
                repeatDigits.put(i, digits[i]);
            }
            usedDigits.add(digits[i]);
        }

        if (!repeatDigits.isEmpty()) {
            int nextNumber = 0;
            for (Map.Entry<Integer, Character> entry : repeatDigits.entrySet()) {
                int genAttempt = 0;
                char nextNum = ' ';

                do {
                    genAttempt++;
                    int num = genAttempt > 4 ? nextNumber++ : random.nextInt(0, 9);
                    nextNum = (char) (num + '0');
                } while (usedDigits.contains(nextNum));

                usedDigits.add(nextNum);
                digits[entry.getKey()] = nextNum;
            }
        }

        return digits;
    }

    @SneakyThrows
    @Override
    @Transactional
    public void handle(@NotNull QuestBullsAndCows quest, Update update) {
        Advent advent = quest.getStep().getAdvent();
        AdventCurrentStep adventCurrentStep = adventCurrentStepService.findById(advent.getId());
        Pair<Integer, Integer> result = checkAnswer(adventCurrentStep.getData(), MessageUtils.getMessageText(update));
        incrementNumberOfAttempts(adventCurrentStep.getData());
        int numberOfAttempts = getNumberOfAttempts(adventCurrentStep.getData());

        SendMessage message = SendMessage.builder()
                .chatId(advent.getChatId())
                .text(String.format("Попытка %s: быков \uD83D\uDC02 %s, коров \uD83D\uDC04 %s",
                        numberOfAttempts + 1,
                        result.getLeft(),
                        result.getRight()))
                .build();
        telegramClient.execute(message);

        if (result.getLeft() == 4) {
            message = SendMessage.builder()
                    .chatId(advent.getChatId())
                    .text("Задача решена! ✔\uFE0F")
                    .build();
            telegramClient.execute(message);

            stepService.handleNextSteps(
                    advent,
                    adventCurrentStep.getStep().getDay(),
                    adventCurrentStep.getStep().getOrder()
            );
        } else {
            adventCurrentStepService.save(adventCurrentStep);
        }
    }

    @Override
    public Class<QuestBullsAndCows> getHandledQuestClass() {
        return QuestBullsAndCows.class;
    }

    private @NonNull Pair<Integer, Integer> checkAnswer(Map<String, Object> data, String messageText) {
        if (StringUtils.length(messageText) != 4) {
            throw new AppException("В сообщении ожидаются 4 цифры");
        }

        if (!StringUtils.isNumeric(messageText)) {
            throw new AppException("В сообщении ожидаются 4 цифры");
        }

        char[] entered = messageText.toCharArray();
        char[] guessWord = getGuessWord(data);

        int bullsCount = 0;
        for (int i = 0; i < 4; i++) {
            if (entered[i] == guessWord[i]) {
                bullsCount++;
            }
        }

        if (bullsCount == 4) {
            return Pair.of(4, 0);
        }

        Set<Character> intersection = new HashSet<>(formSet(guessWord));
        intersection.retainAll(formSet(entered));
        int cowsCount = intersection.size() - bullsCount;
        return Pair.of(bullsCount, cowsCount);
    }

    private Set<Character> formSet(char[] symbols) {
        Set<Character> result = new HashSet<>(4);
        for (char symbol : symbols) {
            result.add(symbol);
        }

        return result;
    }

    private QuestType getQuestType() {
        return BULLS_AND_COWS;
    }

    @Override
    public boolean canHandle(Integer questType) {
        return getQuestType().is(questType);
    }

    @Override
    @Transactional(readOnly = true)
    public String getMessageForCreate() {
        String questType = clsQuestTypeService.getQuestTypeName(getQuestType().getId());
        return "Для добавления шага (" + questType + ") введите:\n" +
                """
                        день,
                        порядок шага (оставьте строку пустой - порядок будет максимальный в рамках дня),
                        текст без переносов строки (если не нужен, то оставьте пустую строку).
                        Каждые новые данные вводятся с новой строки. Порядок важен.
                        Пример,
                        1
                        1
                        Привет, это игра быки и коровы. Тебе нужно угадать число из 4 неповторяющихся цифр. Если угадал цифру, но не угадал её позицию, то это корова. Если угадал и цифру, и позицию - то это бык.
                        """;
    }

    @Override
    @Transactional
    public Long createStep(Update update) {
        long personId = getTelegramUserId(update);
        Pair<Integer, Integer> ids = adminProgressService.getAdventStepsCreateIds(personId);
        Step step = createStep(MessageUtils.getMessageText(update), ids.getLeft());
        stepRepository.save(step);
        return step.getId();
    }

    private Step createStep(String input, @NonNull Integer adventId) {
        if (input == null) {
            throw new AppException("Нет данных для создания шага");
        }

        String[] data = input.split("\n");
        if (data.length != EXPECTED_ROWS) {
            throw new AppException("Ожидаются данные на " + EXPECTED_ROWS + " строчках");
        }

        Step step = stepCommon.createStep(data, adventId);
        QuestBullsAndCows quest = new QuestBullsAndCows();
        quest.setStep(step);
        step.getQuests().add(quest);

        return step;
    }
}
