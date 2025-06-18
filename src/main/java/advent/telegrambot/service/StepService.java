package advent.telegrambot.service;

import advent.telegrambot.domain.AdventCurrentStep;
import advent.telegrambot.domain.Step;
import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.repository.AdventCurrentStepRepository;
import advent.telegrambot.repository.StepRepository;
import advent.telegrambot.utils.AppException;
import advent.telegrambot.utils.MessageUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class StepService {
    private final StepRepository stepRepository;

    @Transactional(readOnly = true)
    public List<Step> getNextSteps(@NonNull Advent advent, @NonNull Short day, @NonNull Short order) {
        List<Step> nextSteps = stepRepository.findByAdventAndDayAndOrderGreaterThanOrderByOrderAsc(
                advent,
                day,
                order);

        int index = IntStream.range(0, nextSteps.size())
                .filter(i -> !nextSteps.get(i).getQuests().isEmpty())
                .findFirst()
                .orElse(nextSteps.size() - 1);
        return nextSteps.subList(0, index + 1);
    }

    @Transactional(readOnly = true)
    public @NonNull Step getById(@NonNull Long stepId) {
        return stepRepository.findById(stepId).orElseThrow(() -> new AppException("Шаг с идентификатором " + stepId + " не найден"));
    }

    @Transactional
    public void save(Step step) {
        stepRepository.save(step);
        reopenAdventIfNeed(step.getAdvent());
    }

    private void reopenAdventIfNeed(Advent advent) {
        if (advent.getFinishDate() != null &&
                advent.getStartDate().plusDays(stepRepository.getMaxDaysByAdvent(advent) - 1).isAfter(advent.getFinishDate())) {
            advent.setFinishDate(null);
        }
    }
}
