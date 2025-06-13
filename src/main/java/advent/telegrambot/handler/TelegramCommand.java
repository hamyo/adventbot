package advent.telegrambot.handler;

import advent.telegrambot.utils.MessageUtils;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

@Getter
public enum TelegramCommand {
    START("/start"),
    SET_CHAT_ID("/setchatid"),
    HINT("/помоги", "помоги"),

    ADVENTS("/advents", "Незавершенные адвенты"),
    ADVENTS_CREATE("/advents/create", "Создать новый"),
    ADVENTS_EDIT("/advents/{id}", "Редактировать", "^\\/advents\\/(\\d+)$"),
    ADVENTS_PERSONS("/advents/{id}/persons", "Участники", "^\\/advents\\/(\\d+)\\/persons$"),
    ADVENTS_STEPS("/advents/{id}/steps", "Шаги", "^\\/advents\\/(\\d+)\\/steps$"),
    ADVENTS_CODES("/advents/{id}/codes", "Коды адвента", "^\\/advents\\/(\\d+)\\/codes"),
    ADVENTS_FULL_INFO("/advents/{id}/fullinfo", "Полная информация", "^\\/advents\\/(\\d+)\\/fullinfo"),
    ADVENTS_STEPS_CREATE("/advents/{id}/steps/create", "Создание шага адвента", "^\\/advents\\/(\\d+)\\/steps\\/create(\\?questTypeId=\\d+)?$"),
    ADVENTS_STEPS_CREATED("/advents/{id}/steps/created", "Шаг создан"),

    ADMIN_ADD("/admin/add"),
    ADMIN_CLEAR("/admin/clear"),
    ;

    private static final String COMMAND_SEPARATOR = "?";

    private final String action;
    private String rusName;
    private Pattern regexPattern = null;

    TelegramCommand(String action) {
        this.action = action;
    }

    TelegramCommand(String action, String rusName) {
        this.action = action;
        this.rusName = rusName;
    }

    TelegramCommand(String action, String rusName, String regexPattern) {
        this.action = action;
        this.rusName = rusName;
        this.regexPattern = Pattern.compile(regexPattern);
    }

    public static boolean isNotAnyCommand(Update update) {
        String actionName = StringUtils.substringBefore(MessageUtils.getMessageText(update), COMMAND_SEPARATOR);
        return Arrays.stream(TelegramCommand.values())
                .noneMatch(command -> command.isMatch(actionName));
    }

    private boolean isMatch(String actionName) {
        return (regexPattern == null && equalsIgnoreCase(action, actionName)) ||
                (regexPattern != null && regexPattern.matcher(actionName).matches());
    }

    public boolean is(Update update) {
        String actionName = StringUtils.substringBefore(MessageUtils.getMessageText(update), COMMAND_SEPARATOR);
        return isMatch(actionName);
    }

    public boolean isNot(Update update) {
        return !is(update);
    }

    public boolean isStrict(Update update) {
        String messageText = MessageUtils.getMessageText(update);
        return isMatch(messageText);
    }

    public static String getEditCommand(Integer adventId) {
        return getCommandWithId(adventId, TelegramCommand.ADVENTS_EDIT);
    }

    private static String getCommandWithId(@NonNull Integer id, @NonNull TelegramCommand command) {
        return command.action.replace("{id}", id.toString());
    }

    public @NonNull Integer getIdFromAction(@NonNull String inAction) {
        if (regexPattern == null) {
            throw new UnsupportedOperationException("Not supported");
        }

        Matcher matcher = regexPattern.matcher(inAction);
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        } else {
            throw new IllegalArgumentException("inAction=" + inAction + " not matches regexPattern=" + regexPattern.pattern());
        }
    }

    public static String getAdventPersonCommand(@NonNull Integer adventId) {
        return getCommandWithId(adventId, TelegramCommand.ADVENTS_PERSONS);
    }

    public static String getAdventStepCommand(@NonNull Integer adventId) {
        return getCommandWithId(adventId, TelegramCommand.ADVENTS_STEPS);
    }

    public static String getAdventFullInfoCommand(@NonNull Integer adventId) {
        return getCommandWithId(adventId, TelegramCommand.ADVENTS_FULL_INFO);
    }

    public static String getAdventCodeCommand(@NonNull Integer adventId) {
        return getCommandWithId(adventId, TelegramCommand.ADVENTS_CODES);
    }

    public static String getAdventStepCreateCommand(@NonNull Integer adventId, Integer questTypeId) {
        return getCommandWithId(adventId, TelegramCommand.ADVENTS_STEPS_CREATE) +
                (questTypeId == null ? "" : COMMAND_SEPARATOR + "questTypeId=" + questTypeId);
    }

    public static @NonNull Pair<Integer, Integer> getIdsFromStepCreateCommand(@NonNull String inAction) {
        Matcher matcher = TelegramCommand.ADVENTS_STEPS_CREATE.regexPattern.matcher(inAction);
        if (matcher.matches()) {
            return Pair.of(
                    Integer.parseInt(matcher.group(1)),
                    matcher.groupCount() == 2 ? null : Integer.parseInt(matcher.group(2)));
        } else {
            throw new IllegalArgumentException("inAction=" +
                    inAction +
                    " not matches regexPattern=" +
                    TelegramCommand.ADVENTS_STEPS_CREATE.regexPattern.pattern());
        }
    }
}
