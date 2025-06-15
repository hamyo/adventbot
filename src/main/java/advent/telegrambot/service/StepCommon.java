package advent.telegrambot.service;

import advent.telegrambot.classifier.DataType;
import advent.telegrambot.domain.Content;
import advent.telegrambot.domain.Hint;
import advent.telegrambot.domain.Step;
import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.domain.quest.Quest;
import advent.telegrambot.repository.StepRepository;
import advent.telegrambot.utils.AppException;
import advent.telegrambot.utils.NumberUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.ByteArrayInputStream;
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
}
