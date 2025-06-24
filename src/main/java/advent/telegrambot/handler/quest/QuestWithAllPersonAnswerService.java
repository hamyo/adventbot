package advent.telegrambot.handler.quest;

import advent.telegrambot.classifier.DataType;
import advent.telegrambot.classifier.QuestType;
import advent.telegrambot.domain.AdventCurrentStep;
import advent.telegrambot.domain.Person;
import advent.telegrambot.domain.Step;
import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.domain.quest.QuestWithAllPersonAnswer;
import advent.telegrambot.handler.StepCreateHandler;
import advent.telegrambot.repository.StepRepository;
import advent.telegrambot.service.*;
import advent.telegrambot.utils.AppException;
import advent.telegrambot.utils.MessageUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Set;

import static advent.telegrambot.classifier.QuestType.ALL_PERSON_ANSWER;
import static advent.telegrambot.utils.MessageUtils.getTelegramUserId;


@Service
@RequiredArgsConstructor
public class QuestWithAllPersonAnswerService implements StepCreateHandler {
    private final ClsDataTypeService clsDataTypeService;
    private final AdminProgressService adminProgressService;
    private final StepRepository stepRepository;
    private final StepCommon stepCommon;
    private final ClsQuestTypeService clsQuestTypeService;
    private final AdventService adventService;
    private final AdventCurrentStepService adventCurrentStepService;

    private final static int EXPECTED_ROWS = 5;

    private QuestType getQuestType() {
        return ALL_PERSON_ANSWER;
    }

    @Override
    public boolean canHandle(Integer questType) {
        return getQuestType().is(questType);
    }

    @Transactional
    public @NonNull Pair<Person, Boolean> checkAnswer(@NonNull Integer adventId, @NonNull Long answeredPersonId) {
        Advent advent = adventService.findById(adventId);

        AdventCurrentStep adventCurrentStep = adventCurrentStepService.findById(advent.getId());
        Person answeredPerson = advent.getPersons().stream()
                .filter(person -> person.getId().equals(answeredPersonId))
                .findAny()
                .orElse(null);

        if (answeredPerson != null) {
            Set<Long> alreadyAnsweredPersonIds = adventCurrentStep.getData().getAlreadyAnsweredPersonIds();
            alreadyAnsweredPersonIds.add(answeredPersonId);

            if (alreadyAnsweredPersonIds.size() == advent.getPersons().size()) {
                return Pair.of(answeredPerson, true);
            }
        }

        return Pair.of(answeredPerson, false);
    }

    @Override
    @Transactional(readOnly = true)
    public String getMessageForCreate() {
        String questType = clsQuestTypeService.getQuestTypeName(getQuestType().getId());
        String dataTypeDescription = clsDataTypeService.getAllDataTypeDescription();
        return "Для добавления шага (" + questType + ") введите:\n" +
                """
                        день,
                        порядок шага (оставьте строку пустой - порядок будет максимальный в рамках дня),
                        текст без переносов строки (если он нужен, если не нужен, то оставьте пустую строку),
                        подсказки на одной строчке, разделенные знаком | (если не нужны, то оставьте пустую строку),
                        идентификаторы возможных типов данных ответа, разделенные знаком |
                        """ +
                dataTypeDescription +
                """
                        .
                        Каждые новые данные вводятся с новой строки. Порядок важен.
                        "Пример,
                        1
                        1
                        Привет, нарисуй новогоднюю открытку и пришли её фотографию
                        
                        1
                        """
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

        Step step = stepCommon.createStep(data, adventId);
        QuestWithAllPersonAnswer quest = new QuestWithAllPersonAnswer();
        quest.setStep(step);
        step.getQuests().add(quest);
        quest.getHints().addAll(stepCommon.parseHints(data[EXPECTED_ROWS - 2], quest));
        quest.setAllowedAnswerTypes(DataType.getIdsFromString(data[EXPECTED_ROWS - 1]));

        return step;
    }
}
