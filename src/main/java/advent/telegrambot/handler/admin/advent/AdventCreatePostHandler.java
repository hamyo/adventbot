package advent.telegrambot.handler.admin.advent;

import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.domain.advent.AdventByCode;
import advent.telegrambot.domain.advent.AdventByMessage;
import advent.telegrambot.handler.MessageHandler;
import advent.telegrambot.handler.advent.AdventHandlerFactory;
import advent.telegrambot.service.AdminProgressService;
import advent.telegrambot.service.AdventService;
import advent.telegrambot.service.PersonService;
import advent.telegrambot.utils.AppException;
import advent.telegrambot.utils.DateUtils;
import advent.telegrambot.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDate;

import static advent.telegrambot.handler.TelegramCommand.ADVENTS_CREATE;
import static advent.telegrambot.utils.MessageUtils.getTelegramUserId;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdventCreatePostHandler implements MessageHandler {
    private final TelegramClient telegramClient;
    private final AdventService adventService;
    private final PersonService personService;
    private final AdminProgressService adminProgressService;
    private final AdventHandlerFactory adventHandlerFactory;

    private Advent createAdventByInput(String input) {
        if (StringUtils.isEmpty(input)) {
            throw new AppException("Данные для адвента не введены");
        }

        String[] data = input.split("\n");
        if (data.length != 3) {
            throw new AppException("Ожидаются данные на 3 строчках. Сейчас " + data.length);
        }

        Advent advent;
        try {
            short type = Short.parseShort(data[1].trim());
            advent = switch (type) {
                case 1 -> new AdventByMessage();
                case 2 -> new AdventByCode();
                default -> throw new AppException("Тип адвента должен быть числом 1 или 2");
            };
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException("Тип адвента должен быть числом 1 или 2", e);
        }

        try {
            advent.setStartDate(LocalDate.parse(data[0].trim(), DateUtils.getRusDateFormatter()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException("Дата должна быть в формате дд.ММ.гггг", e);
        }

        advent.setHelloMessage(data[2].trim());
        return advent;
    }

    @SneakyThrows
    @Override
    public void handle(Update update) {
        Advent newAdvent = createAdventByInput(MessageUtils.getMessageText(update));
        Integer adventId = adventService.create(newAdvent);
        adminProgressService.delete(getTelegramUserId(update));
        SendMessage response = SendMessage.builder()
                .chatId(MessageUtils.getChatId(update))
                .text("Адвент успешно создан. Теперь нужно ввести дополнительные данные для него")
                .replyMarkup(adventHandlerFactory.getAdminKeyboard(adventService.findById(adventId)))
                .build();
        telegramClient.execute(response);

    }

    @Override
    public boolean canHandle(Update update) {
        long personId = getTelegramUserId(update);
        return personService.isAdmin(getTelegramUserId(update)) &&
                adminProgressService.isCurrentCommand(personId, ADVENTS_CREATE);
    }
}
