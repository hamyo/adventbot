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
    private final StepCommon stepCommon;
    private final TelegramClient telegramClient;
    private final AdventCurrentStepRepository adventCurrentStepRepository;

    @Transactional
    public void handleStartDayStep(@NonNull Advent advent, @NonNull Short day) {
        handleNextSteps(advent, day, (short) 0);
    }

    @Transactional
    public void handleNextSteps(@NonNull Advent advent, @NonNull Short day, @NonNull Short order) {
        List<Step> steps = getNextSteps(advent, day, order);
        steps.forEach(step -> this.handle(step, advent));
        if (steps.isEmpty()) {
            Step lastStep = steps.getLast();
            if (!stepRepository.existsNextSteps(
                    lastStep.getAdvent().getId(),
                    lastStep.getDay(),
                    lastStep.getOrder())) {
                lastStep.getAdvent().setFinishDate(LocalDate.now());
            }
        }
    }

    @SneakyThrows
    private void handle(Step step, @NonNull Advent advent) {
        InlineKeyboardMarkup markup = step.getQuests().isEmpty() || step.getQuests().getFirst().getHints().isEmpty() ?
                null :
                MessageUtils.getHintActionKeyboard();

        stepCommon.sendContentMessage(step.getContent(), advent.getChatId(), markup);
        if (StringUtils.isNoneBlank(step.getText())) {
            SendMessage message = SendMessage // Create a message object
                    .builder()
                    .chatId(advent.getChatId())
                    .replyMarkup(markup)
                    .text(step.getText())
                    .build();
            telegramClient.execute(message);

            adventCurrentStepRepository.save(new AdventCurrentStep(
                    advent.getId(),
                    step
            ));
        }
    }

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
