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
public class QuestBullsAndCowsService implements StepCreateHandler {
    private final AdventCurrentStepService adventCurrentStepService;
    private final StepRepository stepRepository;
    private final AdminProgressService adminProgressService;
    private final StepCommon stepCommon;
    private final ClsQuestTypeService clsQuestTypeService;

    private final static String GUESS_WORD = "GUESS_WORD";
    private final static String NUMBER_OF_ATTEMPTS = "NUMBER_OF_ATTEMPTS";
    private final static int EXPECTED_ROWS = 3;
    private final static int SYMBOLS_COUNT = 4;

    private int getNumberOfAttempts(Map<String, Object> data) {
        return (int) data.getOrDefault(NUMBER_OF_ATTEMPTS, 0);
    }

    private int incrementNumberOfAttempts(Map<String, Object> data) {
        int attempt = getNumberOfAttempts(data);
        data.put(NUMBER_OF_ATTEMPTS, attempt + 1);
        return attempt + 1;
    }

    private char[] getGuessWord(Map<String, Object> data) {
        char[] word = (char[]) data.get(GUESS_WORD);
        if (word == null) {
            word = generateNumber();
            data.put(GUESS_WORD, word);
        }

        return word;
    }

    private char[] generateNumber() {
        SplittableRandom random = new SplittableRandom();
        int number = random.nextInt(123, 9876);
        char[] digits = StringUtils.leftPad(String.valueOf(number), SYMBOLS_COUNT, "0").toCharArray();

        Set<Character> usedDigits = new HashSet<>(SYMBOLS_COUNT);
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
                    int num = genAttempt > SYMBOLS_COUNT ? nextNumber++ : random.nextInt(0, 9);
                    nextNum = (char) (num + '0');
                } while (usedDigits.contains(nextNum));

                usedDigits.add(nextNum);
                digits[entry.getKey()] = nextNum;
            }
        }

        return digits;
    }

    @Transactional
    public @NonNull BullsAndCowsResult checkAnswer(@NonNull Integer adventId, String messageText) {
        if (StringUtils.length(messageText) != SYMBOLS_COUNT) {
            throw new AppException("В сообщении ожидаются " + SYMBOLS_COUNT + " цифры");
        }

        if (!StringUtils.isNumeric(messageText)) {
            throw new AppException("В сообщении ожидаются " + SYMBOLS_COUNT + " цифры");
        }

        AdventCurrentStep adventCurrentStep = adventCurrentStepService.findById(adventId);
        int attemptCount = incrementNumberOfAttempts(adventCurrentStep.getData());

        char[] entered = messageText.toCharArray();
        char[] guessWord = getGuessWord(adventCurrentStep.getData());

        int bullsCount = 0;
        for (int i = 0; i < SYMBOLS_COUNT; i++) {
            if (entered[i] == guessWord[i]) {
                bullsCount++;
            }
        }

        if (bullsCount == SYMBOLS_COUNT) {
            return BullsAndCowsResult.finished(bullsCount, 0, attemptCount);
        }

        Set<Character> intersection = new HashSet<>(formSet(guessWord));
        intersection.retainAll(formSet(entered));
        int cowsCount = intersection.size() - bullsCount;

        return BullsAndCowsResult.notFinished(bullsCount, cowsCount, attemptCount);
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
