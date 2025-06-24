package advent.telegrambot.service;

import advent.telegrambot.classifier.DataType;
import advent.telegrambot.domain.AdventCurrentStep;
import advent.telegrambot.domain.Content;
import advent.telegrambot.domain.Hint;
import advent.telegrambot.domain.Step;
import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.domain.quest.Quest;
import advent.telegrambot.repository.AdventCurrentStepRepository;
import advent.telegrambot.repository.StepRepository;
import advent.telegrambot.utils.AppException;
import advent.telegrambot.utils.MessageUtils;
import advent.telegrambot.utils.NumberUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@RequiredArgsConstructor
public class StepCommon {
    private final TelegramClient telegramClient;
    private final StepRepository stepRepository;
    private final AdventService adventService;
    private final StepService stepService;
    private final AdventCurrentStepService adventCurrentStepService;

    public @NonNull Short getStepOrder(String order, @NonNull Advent advent, @NonNull Short day) {
        if (StringUtils.isNotBlank(order)) {
            return NumberUtils.parseShort(order.trim(), "Порядок");
        }

        return (short) (stepRepository.getMaxOrderAtDayByAdvent(advent, day) + 1);
    }

    @SneakyThrows
    public void sendContentMessage(Content content, long chatId, InlineKeyboardMarkup markup) {
        if (content == null) {
            return;
        }

        if (DataType.IMAGE.equals(content.getType())) {
            SendPhoto message = SendPhoto.builder()
                    .chatId(chatId)
                    .replyMarkup(markup)
                    .caption(content.getCaption())
                    .photo(new InputFile(
                            new ByteArrayInputStream(content.getData()),
                            content.getNotEmptyName()))
                    .build();
            telegramClient.execute(message);
        } else if (DataType.DOCUMENT.equals(content.getType())) {
            SendDocument message = SendDocument.builder()
                    .chatId(chatId)
                    .replyMarkup(markup)
                    .caption(content.getCaption())
                    .document(new InputFile(
                            new ByteArrayInputStream(content.getData()),
                            content.getNotEmptyName()))
                    .build();
            telegramClient.execute(message);
        } else if (DataType.AUDIO.equals(content.getType())) {
            SendAudio message = SendAudio.builder()
                    .chatId(chatId)
                    .replyMarkup(markup)
                    .caption(content.getCaption())
                    .audio(new InputFile(
                            new ByteArrayInputStream(content.getData()),
                            content.getNotEmptyName()))
                    .build();
            telegramClient.execute(message);
        } else if (DataType.VOICE.equals(content.getType())) {
            SendVoice message = SendVoice.builder()
                    .chatId(chatId)
                    .replyMarkup(markup)
                    .caption(content.getCaption())
                    .voice(new InputFile(
                            new ByteArrayInputStream(content.getData()),
                            content.getNotEmptyName()))
                    .build();
            telegramClient.execute(message);
        } else if (DataType.VIDEO.equals(content.getType()) || DataType.GIF.equals(content.getType())) {
            SendVideo message = SendVideo.builder()
                    .chatId(chatId)
                    .replyMarkup(markup)
                    .caption(content.getCaption())
                    .video(new InputFile(
                            new ByteArrayInputStream(content.getData()),
                            content.getNotEmptyName()))
                    .build();
            telegramClient.execute(message);
        }
    }

    public void check(Step step) {
        Optional<Step> otherStep = stepRepository.findFirstByAdventAndDayAndOrder(step.getAdvent(), step.getDay(), step.getOrder());
        if (otherStep.isPresent()) {
            throw new AppException("Для данного адвента в день " + step.getDay()  + " уже есть шаг с порядком " + step.getOrder());
        }
    }

    public Step createStep(String[] data, Integer adventId) {
        Advent advent = adventService.findById(adventId);

        Step step = new Step();
        step.setDay(NumberUtils.parseShort(data[0], "День"));
        step.setOrder(getStepOrder(data[1], advent, step.getDay()));
        if (isNotBlank(data[2])) {
            step.setText(data[2]);
        }

        step.setAdvent(advent);
        check(step);
        return step;
    }

    public List<Hint> parseHints(String input, Quest quest) {
        if (StringUtils.isBlank(input)) {
            return Collections.emptyList();
        }

        return Arrays.stream(input.split("\\|"))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(Hint::new)
                .peek(hint -> hint.setQuest(quest))
                .toList();
    }

    public void handleStartDayStep(@NonNull Advent advent, @NonNull Short day) {
        handleNextSteps(advent, day, (short) 0);
    }

    public void handleNextSteps(@NonNull Advent advent) {
        Step currentStep = adventCurrentStepService.findById(advent.getId()).getStep();
        handleNextSteps(advent, currentStep.getDay(), currentStep.getOrder());
    }

    public void handleNextSteps(@NonNull Advent advent, @NonNull Short day, @NonNull Short order) {
        List<Step> steps = stepService.getNextSteps(advent, day, order);
        steps.forEach(step -> this.handle(step.getId(), advent));
        // Это нужно при обработке последнего шага
        if (!steps.isEmpty()) {
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
    private void handle(@NonNull Long stepId, @NonNull Advent advent) {
        Step step = stepService.findFullGraphById(stepId);
        InlineKeyboardMarkup markup = step.getQuests().isEmpty() || step.getQuests().getFirst().getHints().isEmpty() ?
                null :
                MessageUtils.getHintActionKeyboard();

        sendContentMessage(step.getContent(), advent.getChatId(), markup);
        if (StringUtils.isNoneBlank(step.getText())) {
            SendMessage message = SendMessage // Create a message object
                    .builder()
                    .chatId(advent.getChatId())
                    .replyMarkup(markup)
                    .text(step.getText())
                    .build();
            telegramClient.execute(message);
        }

        adventCurrentStepService.save(new AdventCurrentStep(
                advent.getId(),
                step
        ));
    }
}
