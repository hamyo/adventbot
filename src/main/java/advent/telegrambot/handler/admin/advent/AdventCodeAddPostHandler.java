package advent.telegrambot.handler.admin.advent;

import advent.telegrambot.handler.MessageHandler;
import advent.telegrambot.handler.TelegramCommand;
import advent.telegrambot.handler.advent.AdventByCodeService;
import advent.telegrambot.service.AdminProgressService;
import advent.telegrambot.service.PersonService;
import advent.telegrambot.utils.MessageUtils;
import advent.telegrambot.utils.NumberUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static advent.telegrambot.handler.TelegramCommand.ADVENTS_CODES_ADD;

@Component
@RequiredArgsConstructor
public class AdventCodeAddPostHandler implements MessageHandler {
    private final PersonService personService;
    private final TelegramClient telegramClient;
    private final AdminProgressService adminProgressService;
    private final AdventByCodeService adventByCodeService;

    private short getDaysCount(String text) {
        return NumberUtils.parseShort(text, "Количество дней");
    }

    @SneakyThrows
    @Override
    public void handle(Update update) {
        short daysCount = getDaysCount(MessageUtils.getMessageText(update));
        long personId = MessageUtils.getTelegramUserId(update);
        Integer adventId = adminProgressService.getAdventId(personId);
        adventByCodeService.addCodes(adventId, daysCount);

        SendMessage message = SendMessage.builder()
                .chatId(MessageUtils.getChatId(update))
                .text("Коды успешно обновлены")
                .replyMarkup(MessageUtils.getAdminCodesActionKeyboard(adventId))
                .build();
        telegramClient.execute(message);
        adminProgressService.delete(personId);
    }

    @Override
    public boolean canHandle(Update update) {
        long personId = MessageUtils.getTelegramUserId(update);
        return adminProgressService.isCurrentCommand(personId, ADVENTS_CODES_ADD) &&
                TelegramCommand.isNotAnyCommand(update) &&
                personService.isAdmin(personId);
    }
}
