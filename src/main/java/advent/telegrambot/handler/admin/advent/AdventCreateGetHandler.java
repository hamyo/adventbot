package advent.telegrambot.handler.admin.advent;

import advent.telegrambot.handler.MessageHandler;
import advent.telegrambot.service.AdminProgressService;
import advent.telegrambot.service.PersonService;
import advent.telegrambot.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Arrays;
import java.util.Collections;

import static advent.telegrambot.handler.TelegramCommand.ADVENTS;
import static advent.telegrambot.handler.TelegramCommand.ADVENTS_CREATE;
import static advent.telegrambot.utils.MessageUtils.getTelegramUserId;

@Component
@RequiredArgsConstructor
public class AdventCreateGetHandler implements MessageHandler {
    private final TelegramClient telegramClient;
    private final PersonService personService;
    private final AdminProgressService adminProgressService;

    @SneakyThrows
    @Override
    public void handle(Update update) {
        adminProgressService.save(getTelegramUserId(update), ADVENTS_CREATE);

        SendMessage response = SendMessage.builder()
                .chatId(MessageUtils.getChatId(update))
                .text("""
                        Для создания нового адвента укажите дату начала, тип: обычный (1) или с кодом (2) и приветственное сообщение.
                        Каждые данные указывайте на новой строчке. Порядок важен. Не используйте перенос в приветственном сообщении.
                        Пример,
                        01.12.2025
                        1
                        Привет, это бот для адвента.
                        """)
                .replyMarkup(getInlineKeyboardMarkup())
                .build();
        telegramClient.executeAsync(response);
    }

    private InlineKeyboardMarkup getInlineKeyboardMarkup() {
        return new InlineKeyboardMarkup(Collections.singletonList(new InlineKeyboardRow(
                Arrays.asList(
                        InlineKeyboardButton.builder()
                                .text(ADVENTS.getRusName())
                                .callbackData(ADVENTS.getAction())
                                .build()))));
    }

    @Override
    public boolean canHandle(Update update) {
        return ADVENTS_CREATE.isStrict(update) &&
                personService.isAdmin(getTelegramUserId(update));
    }
}
