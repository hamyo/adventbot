package advent.telegrambot.handler.quest;

import advent.telegrambot.domain.AdventCurrentStep;
import advent.telegrambot.classifier.DataType;
import advent.telegrambot.domain.Person;
import advent.telegrambot.domain.Step;
import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.domain.quest.Quest;
import advent.telegrambot.domain.quest.QuestWithAllPersonAnswer;
import advent.telegrambot.handler.StepCreateHandler;
import advent.telegrambot.repository.ClsDataTypeRepository;
import advent.telegrambot.repository.ClsQuestTypeRepository;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static advent.telegrambot.classifier.QuestType.ALL_PERSON_ANSWER;
import static advent.telegrambot.utils.MessageUtils.getTelegramUserId;


@Service
@RequiredArgsConstructor
public class QuestWithAllPersonAnswerHandler implements QuestHandler<QuestWithAllPersonAnswer>, StepCreateHandler {
    private final TelegramClient telegramClient;
    private final AdventCurrentStepService adventCurrentStepService;
    private final StepService stepService;
    private final ClsDataTypeRepository clsDataTypeRepository;
    private final AdminProgressService adminProgressService;
    private final AdventService adventService;
    private final StepRepository stepRepository;
    private final StepCommonService stepCommonService;
    private final ClsQuestTypeRepository clsQuestTypeRepository;

    private final static String ALREADY_ANSWERED_PERSON_IDS = "ALREADY_ANSWERED_PERSON_IDS";
    private final static int EXPECTED_ROWS = 5;

    private Set<Long> getAlreadyAnsweredPersonIds(Map<String, Object> adventCurrentStepData) {
        return Arrays.stream(
                        String.valueOf(adventCurrentStepData.getOrDefault(ALREADY_ANSWERED_PERSON_IDS, "")).split(","))
                .map(String::trim)
                .filter(StringUtils::isNumeric)
                .map(Long::parseLong)
                .collect(Collectors.toSet());
    }

    private void updateCurrentStep(AdventCurrentStep currentStep, Set<Long> alreadyAnsweredPersonIds) {
        currentStep.getData().put(
                ALREADY_ANSWERED_PERSON_IDS,
                alreadyAnsweredPersonIds.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(","))
        );
        adventCurrentStepService.save(currentStep);
    }

    @SneakyThrows
    @Override
    @Transactional
    public void handle(@NotNull QuestWithAllPersonAnswer quest, Update update) {
        DataType answerType = DataType.of(update);
        if (answerType == null || quest.isNotNeedType(answerType)) {
            throw new AppException("Это не тот ответ, который ожидается\uD83D\uDE1F. Я жду " + quest.getRusNameNeedTypes());
        }

        Advent advent = quest.getStep().getAdvent();
        AdventCurrentStep adventCurrentStep = adventCurrentStepService.findById(advent.getId());
        Set<Long> alreadyAnsweredPersonIds = getAlreadyAnsweredPersonIds(adventCurrentStep.getData());
        Long answeredPersonId = MessageUtils.getTelegramUserId(update);
        Person answeredPerson = advent.getPersons().stream()
                .filter(person -> person.getId().equals(answeredPersonId))
                .findAny()
                .orElse(null);
        if (answeredPerson != null) {
            alreadyAnsweredPersonIds.add(answeredPersonId);

            SendMessage message = SendMessage
                    .builder()
                    .chatId(advent.getChatId())
                    .text(MessageUtils.getResponseTextForUser(answeredPerson.getNameNominative()))
                    .build();
            telegramClient.executeAsync(message);


            updateCurrentStep(adventCurrentStep, alreadyAnsweredPersonIds);
            if (alreadyAnsweredPersonIds.size() == advent.getPersons().size()) {
                stepService.handleNextSteps(
                        advent,
                        quest.getStep().getDay(),
                        quest.getStep().getOrder()
                );
            }
        }
    }

    @Override
    public Class<QuestWithAllPersonAnswer> getHandledQuestClass() {
        return QuestWithAllPersonAnswer.class;
    }

    @Override
    public boolean canHandle(Integer questType) {
        return ALL_PERSON_ANSWER.is(questType);
    }

    @Override
    @Transactional(readOnly = true)
    public String getMessageForCreate() {
        String dataTypeDescription = clsDataTypeRepository.findAll().stream()
                .map(dataType -> String.format("%s (%s)", dataType.getId(), dataType.getName()))
                .collect(Collectors.joining(",", "(", ")"));
        return "Для добавления создания шага введите день, порядок шага (оставьте строку пустой - порядок будет максимальный в рамках дня), текст без переносов строки (если он нужен, если не нужен, то оставьте пустую строку), " +
                "подсказки на одной строчке, разделенные знаком | (если не нужны, то оставьте пустую строку), " +
                "идентификаторы возможных типов данных ответа, разделенные знаком | " +
                dataTypeDescription +
                ". Каждые новые данные вводятся с новой строки. Порядок важен.\n" +
                "Пример,\n" +
                "1\n" +
                "1\n" +
                "Привет, нарисуй новогоднюю открытку и пришли её фотографию\n" +
                "\n" +
                "1"
                ;
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

        Step step = stepCommonService.createStep(data, adventId);
        QuestWithAllPersonAnswer quest = new QuestWithAllPersonAnswer();
        quest.setStep(step);
        step.setQuests(Collections.singletonList(quest));
        quest.setHints(stepCommonService.parseHints(data[EXPECTED_ROWS - 2], quest));
        quest.setAllowedAnswerTypes(DataType.getIdsFromString(data[EXPECTED_ROWS - 1]));

        return step;
    }
}
