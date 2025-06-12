package advent.telegrambot.handler;

import advent.telegrambot.domain.AdventCurrentStep;
import advent.telegrambot.domain.Content;
import advent.telegrambot.domain.Hint;
import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.domain.quest.Quest;
import advent.telegrambot.service.AdventCurrentStepService;
import advent.telegrambot.service.AdventService;
import advent.telegrambot.service.PersonService;
import advent.telegrambot.service.StepCommonService;
import advent.telegrambot.utils.MessageUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Comparator;
import java.util.Map;

import static advent.telegrambot.utils.MessageUtils.getHintActionKeyboard;

@Service
@RequiredArgsConstructor
public class HintHandler implements MessageHandler {
    private final TelegramClient telegramClient;
    private final PersonService personService;
    private final AdventService adventService;
    private final AdventCurrentStepService adventCurrentStepService;
    private final StepCommonService stepCommonService;

    private final static String SHOWED_HINT_ID = "SHOWED_HINT_ID";

    private @NonNull Long getShowedHintId(@NonNull Map<String, Object> data) {
        return (Long) data.getOrDefault(SHOWED_HINT_ID, -1L);
    }

    private void saveShowedHintId(@NonNull AdventCurrentStep currentStep, @NonNull Long showedHintId) {
        Map<String, Object> data = currentStep.getData();
        data.put(SHOWED_HINT_ID, showedHintId);
        adventCurrentStepService.save(currentStep);
    }

    @SneakyThrows
    @Override
    public void handle(Update update) {
        long chatId = MessageUtils.getChatId(update);
        Advent advent = adventService.findByChatId(chatId);
        AdventCurrentStep currentStep = adventCurrentStepService.findById(advent.getId());
        Quest quest = currentStep.getStep().getQuests().isEmpty() ?
                null :
                currentStep.getStep().getQuests().getFirst();

        if (quest == null || quest.getHints().isEmpty()) {
            SendMessage message = SendMessage
                    .builder()
                    .chatId(advent.getChatId())
                    .text("Здесь я не знаю, что подсказать \uD83D\uDE28 ")
                    .build();
            telegramClient.execute(message);
            return;
        }

        Long showedHintId = getShowedHintId(currentStep.getData());
        quest.getHints().stream()
                .filter(hint -> hint.getId().compareTo(showedHintId) > 0)
                .findFirst()
                .ifPresentOrElse(
                        (hint) -> {
                            Long maxId = quest.getHints().stream()
                                    .map(Hint::getId)
                                    .max(Comparator.naturalOrder())
                                    .orElse(0L);

                            Content content = hint.getContent();
                            try {
                                InlineKeyboardMarkup markup = maxId.compareTo(hint.getId()) > 0 ?
                                        getHintActionKeyboard() : null;
                                if (content != null) {
                                    stepCommonService.sendContentMessage(
                                            content,
                                            advent.getChatId(),
                                            markup);
                                }

                                if (StringUtils.isNotBlank(hint.getText())) {
                                    SendMessage message = SendMessage
                                            .builder()
                                            .chatId(advent.getChatId())
                                            .text(hint.getText())
                                            .replyMarkup(markup)
                                            .build();
                                    telegramClient.executeAsync(message);
                                }

                                saveShowedHintId(currentStep, hint.getId());
                            } catch (TelegramApiException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        () -> {
                            SendMessage message = SendMessage
                                    .builder()
                                    .chatId(advent.getChatId())
                                    .text("К сожалению, подсказки закончились \uD83D\uDE28 ")
                                    .build();
                            try {
                                telegramClient.executeAsync(message);
                            } catch (TelegramApiException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );
    }

    @Override
    public boolean canHandle(Update update) {
        return TelegramCommand.HINT.is(update) &&
                personService.isExist(MessageUtils.getTelegramUserId(update));
    }
}
