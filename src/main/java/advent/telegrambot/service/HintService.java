package advent.telegrambot.service;

import advent.telegrambot.domain.AdventCurrentStep;
import advent.telegrambot.domain.Hint;
import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.domain.quest.Quest;
import kotlin.Pair;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class HintService {
    private final AdventCurrentStepService adventCurrentStepService;
    private final AdventService adventService;

    @Transactional
    public void saveShowedHintId(@NonNull Long chatId, @NonNull Long hintId) {
        Advent advent = adventService.findByChatId(chatId);
        AdventCurrentStep currentStep = adventCurrentStepService.findById(advent.getId());
        currentStep.getData().setShowedHintId(hintId);
    }

    @Transactional(readOnly = true)
    public Pair<Boolean, Hint> getNextHint(@NonNull Long chatId) {
        Advent advent = adventService.findByChatId(chatId);
        AdventCurrentStep currentStep = adventCurrentStepService.findById(advent.getId());
        Quest quest = currentStep.getStep().getQuests().isEmpty() ?
                null :
                currentStep.getStep().getQuests().getFirst();

        if (quest == null || quest.getHints().isEmpty()) {
            return null;
        }

        Long showedHintId = currentStep.getData().getNotEmptyShowedHintId();
        return quest.getHints().stream()
                .filter(hint -> hint.getId().compareTo(showedHintId) > 0)
                .findFirst()
                .map(hint -> {
                            Long maxId = quest.getHints().stream()
                                    .map(Hint::getId)
                                    .max(Comparator.naturalOrder())
                                    .orElse(0L);

                            Hibernate.initialize(hint.getContent());
                            return new Pair<>(maxId.compareTo(hint.getId()) > 0, hint);
                        }
                )
                .orElse(null);
    }

}
