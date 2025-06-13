package advent.telegrambot.handler;

import advent.telegrambot.domain.Content;
import advent.telegrambot.domain.Hint;
import advent.telegrambot.service.HintService;
import advent.telegrambot.service.PersonService;
import advent.telegrambot.service.StepCommon;
import advent.telegrambot.utils.MessageUtils;
import kotlin.Pair;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static advent.telegrambot.utils.MessageUtils.getHintActionKeyboard;

@Component
@RequiredArgsConstructor
public class HintHandler implements MessageHandler {
    private final TelegramClient telegramClient;
    private final PersonService personService;
    private final StepCommon stepCommon;
    private final HintService hintService;

    @SneakyThrows
    @Override
    public void handle(Update update) {
        long chatId = MessageUtils.getChatId(update);
        Pair<Boolean, Hint> nextHint = hintService.getNextHint(chatId);
        if (nextHint == null) {
            SendMessage message = SendMessage
                    .builder()
                    .chatId(chatId)
                    .text("Здесь я не знаю, что подсказать \uD83D\uDE28 ")
                    .build();
            telegramClient.execute(message);
        } else {
            Content content = nextHint.getSecond().getContent();
            Hint hint = nextHint.getSecond();
            if (content != null) {
                stepCommon.sendContentMessage(
                        content,
                        chatId,
                        nextHint.getFirst() && StringUtils.isBlank(hint.getText()) ?
                                getHintActionKeyboard() :
                                null);
            }

            if (StringUtils.isNotBlank(hint.getText())) {
                SendMessage message = SendMessage
                        .builder()
                        .chatId(chatId)
                        .text(hint.getText())
                        .replyMarkup(getHintActionKeyboard())
                        .build();
                telegramClient.executeAsync(message);
                hintService.saveShowedHintId(chatId, hint.getId());
            }
        }
    }

    @Override
    public boolean canHandle(Update update) {
        return TelegramCommand.HINT.is(update) &&
                personService.isExist(MessageUtils.getTelegramUserId(update));
    }
}
