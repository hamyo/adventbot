package advent.telegrambot.utils;

import advent.telegrambot.domain.ClsQuestType;
import advent.telegrambot.handler.TelegramCommand;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.*;

import static advent.telegrambot.handler.TelegramCommand.*;


public class MessageUtils {
    private static final List<String> RESPONSE_TEXT_FOR_USER = Arrays.asList(
            "Спасибо, ${name} \uD83E\uDD70",
            "Понял тебя \uD83D\uDE09",
            "${name}, супер \uD83D\uDE1C",
            "Получил твой ответ",
            "${name}, спасибо \uD83E\uDDD0",
            "Круто \uD83D\uDE0A",
            "Оки-доки, ${name} \uD83E\uDD13"
    );

    public static long getTelegramUserId(Message message) {
        Long userId = tryGetUserId(message);
        if (userId == null) {
            throw new AppException("Не получилось определить id пользователя телеграмм");
        }

        return userId;
    }

    public static String getMessageText(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            return update.getMessage().getText();
        }

        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getData();
        }

        return null;
    }

    public static InlineKeyboardMarkup getStepActionKeyboard(
            @NonNull Integer adventId,
            @NonNull List<ClsQuestType> questTypes) {
        List<InlineKeyboardRow> stepButtons = new ArrayList<>(questTypes.size() + 1);
        stepButtons.add(
                new InlineKeyboardRow(
                        Collections.singletonList(
                                InlineKeyboardButton.builder()
                                        .text("Без задания")
                                        .callbackData(TelegramCommand.getAdventStepCreateCommand(
                                                adventId,
                                                null))
                                        .build()
                        )));
        stepButtons.addAll(
                questTypes.stream()
                        .map(questType -> new InlineKeyboardRow(
                                Collections.singletonList(
                                        InlineKeyboardButton.builder()
                                                .text(questType.getName())
                                                .callbackData(TelegramCommand.getAdventStepCreateCommand(
                                                        adventId,
                                                        questType.getId()))
                                                .build()
                                )))
                        .toList()
        );
        stepButtons.add(
                new InlineKeyboardRow(
                        Collections.singletonList(
                                InlineKeyboardButton.builder()
                                        .text("Назад")
                                        .callbackData(TelegramCommand.getEditCommand(adventId))
                                        .build()
                        )));
        return new InlineKeyboardMarkup(stepButtons);
    }

    public static List<InlineKeyboardRow> getAdminActionKeyboard(@NonNull Integer adventId) {
        List<InlineKeyboardRow> menu = new ArrayList<>();
        menu.add(new InlineKeyboardRow(
                Arrays.asList(
                        InlineKeyboardButton.builder()
                                .text(ADVENTS_PERSONS.getRusName())
                                .callbackData(TelegramCommand.getAdventPersonCommand(adventId))
                                .build())));
        menu.add(new InlineKeyboardRow(
                Arrays.asList(
                        InlineKeyboardButton.builder()
                                .text(ADVENTS_STEPS.getRusName())
                                .callbackData(TelegramCommand.getAdventStepCommand(adventId))
                                .build())));
        menu.add(new InlineKeyboardRow(
                Arrays.asList(
                        InlineKeyboardButton.builder()
                                .text(ADVENTS_FULL_INFO.getRusName())
                                .callbackData(TelegramCommand.getAdventFullInfoCommand(adventId))
                                .build())));
        menu.add(new InlineKeyboardRow(
                Arrays.asList(
                        InlineKeyboardButton.builder()
                                .text(ADVENTS.getRusName())
                                .callbackData(ADVENTS.getAction())
                                .build())));
        return menu;
    }

    public static InlineKeyboardMarkup getHintActionKeyboard() {
        InlineKeyboardButton btnHint = InlineKeyboardButton.builder()
                .text(TelegramCommand.HINT.getRusName())
                .callbackData(TelegramCommand.HINT.getAction())
                .build();

        return new InlineKeyboardMarkup(Collections.singletonList(
                new InlineKeyboardRow(btnHint)
        ));
    }

    public static Long tryGetUserId(Message message) {
        if ((message == null || message.getFrom() == null)) {
            return null;
        }

        return message.getFrom().getId();
    }

    public static String tryGetUserName(Message message) {
        if ((message == null || message.getFrom() == null)) {
            return null;
        }

        User user = message.getFrom();

        return StringUtils.isNotBlank(user.getFirstName()) ?
                message.getFrom().getFirstName() :
                message.getFrom().getUserName();

    }

    public static long getChatId(@NonNull Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        }

        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId();
        }

        throw new AppException("Не удалось определить id чата");
    }

    public static long getTelegramUserId(@NonNull Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getFrom().getId();
        }

        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getFrom().getId();
        }

        return 0;
    }

    public static String getResponseTextForUser(String name) {
        SplittableRandom random = new SplittableRandom();
        return RESPONSE_TEXT_FOR_USER.get(random.nextInt(0, RESPONSE_TEXT_FOR_USER.size() - 1))
                .replace("${name}", name);
    }
}
