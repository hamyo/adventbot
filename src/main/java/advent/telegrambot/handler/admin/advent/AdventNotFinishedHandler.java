package advent.telegrambot.handler.admin.advent;

import advent.telegrambot.domain.dto.AdventInfo;
import advent.telegrambot.handler.MessageHandler;
import advent.telegrambot.handler.TelegramCommand;
import advent.telegrambot.service.AdventService;
import advent.telegrambot.service.PersonService;
import advent.telegrambot.utils.DateUtils;
import advent.telegrambot.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AdventNotFinishedHandler implements MessageHandler {
    private final PersonService personService;
    private final AdventService adventService;
    private final TelegramClient telegramClient;

    @SneakyThrows
    @Override
    public void handle(Update update) {
        List<AdventInfo> adventInfo = adventService.getNotFinishedAdvents();
        String message = adventInfo.stream()
                .map(advent -> String.format(
                        "%s id=%s, дата начала '%s', кол-во дней %s",
                        advent.getType(),
                        advent.getId(),
                        advent.getStartDate().format(DateUtils.getRusDateFormatter()),
                        advent.getDaysCount()))
                .collect(Collectors.joining("\n\n"));

        SendMessage response = SendMessage.builder()
                .chatId(MessageUtils.getChatId(update))
                .text(StringUtils.isEmpty(message) ? "Список пуст" : message)
                .replyMarkup(getInlineKeyboardMarkup(adventInfo))
                .build();
        telegramClient.executeAsync(response);
    }

    @NotNull
    private static InlineKeyboardMarkup getInlineKeyboardMarkup(List<AdventInfo> adventInfo) {
        List<InlineKeyboardRow> adventRows = adventInfo.stream()
                .map(advent -> new InlineKeyboardRow(
                        Collections.singletonList(
                                InlineKeyboardButton.builder()
                                        .text(String.format(
                                                "%s %s дней",
                                                advent.getStartDate().format(DateUtils.getRusDateFormatter()),
                                                advent.getDaysCount()))
                                        .callbackData(TelegramCommand.getEditCommand(advent.getId()))
                                        .build()))
                ).toList();

        InlineKeyboardRow createRow = new InlineKeyboardRow(
                Collections.singletonList(
                        InlineKeyboardButton.builder()
                                .text(TelegramCommand.ADVENTS_CREATE.getRusName())
                                .callbackData(TelegramCommand.ADVENTS_CREATE.getAction())
                                .build()));

        List<InlineKeyboardRow> totalRows = new ArrayList<>(adventRows.size() + 1);
        totalRows.add(createRow);
        totalRows.addAll(adventRows);

        InlineKeyboardMarkup replyMarkup = new InlineKeyboardMarkup(totalRows);
        return replyMarkup;
    }

    @Override
    public boolean canHandle(Update update) {
        return TelegramCommand.ADVENTS.is(update) &&
                personService.isAdmin(MessageUtils.getTelegramUserId(update));
    }
}
