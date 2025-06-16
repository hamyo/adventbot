package advent.telegrambot.handler.admin.advent;

import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.domain.dto.CheckError;
import advent.telegrambot.handler.MessageHandler;
import advent.telegrambot.handler.advent.AdventHandlerFactory;
import advent.telegrambot.repository.StepRepository;
import advent.telegrambot.service.AdventFullInfoService;
import advent.telegrambot.service.AdventService;
import advent.telegrambot.service.PersonService;
import advent.telegrambot.utils.MessageUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.stream.Collectors;

import static advent.telegrambot.handler.TelegramCommand.ADVENTS_FULL_INFO;

@RequiredArgsConstructor
@Component
public class AdventFullInfoHandler implements MessageHandler {
    private final PersonService personService;
    private final TelegramClient telegramClient;
    private final AdventService adventService;
    private final AdventFullInfoService adventFullInfoService;
    private final StepRepository stepRepository;
    private final AdventHandlerFactory adventHandlerFactory;

    @SneakyThrows
    @Override
    public void handle(Update update) {
        Long chatId = MessageUtils.getChatId(update);
        Integer adventId = ADVENTS_FULL_INFO.getIdFromAction(MessageUtils.getMessageText(update));

        sendCheckResult(chatId, adventId);
        sendAdventPersons(chatId, adventId);
        sendAdventSteps(chatId, adventId);
    }

    @SneakyThrows
    private void sendAdventPersons(Long chatId, Integer adventId) {
        String message = adventFullInfoService.getAdventPersonsInfo(chatId, adventId);
        telegramClient.execute(
                SendMessage.builder()
                        .chatId(chatId)
                        .text(message)
                        .build());
    }

    private void sendAdventSteps(@NonNull Long chatId, @NonNull Integer adventId) {
        Advent advent = adventService.findById(adventId);
        short maxDays = stepRepository.getMaxDaysByAdvent(advent);
        for (short day = 1; day <= maxDays; day++) {
            byte[] dayInfo = adventFullInfoService.getAdventDayStepInfo(day, advent);
            telegramClient.executeAsync(
                    SendDocument.builder()
                            .chatId(chatId)
                            .caption("День " + day)
                            .document(new InputFile(
                                    new ByteArrayInputStream(dayInfo),
                                    "Информация_по_дню_" + day + ".zip"))
                            .replyMarkup(adventHandlerFactory.getAdminKeyboard(advent))
                            .build());

        }
    }

    private void sendCheckResult(@NonNull Long chatId, @NonNull Integer adventId) throws TelegramApiException {
        List<CheckError> errors = adventService.check(adventId);
        String message = errors.isEmpty() ?
                "Проверка адвента прошла успешно. Проблем не выявлено" :
                "Найденные проблемы:\n" +
                        errors.stream()
                                .map(CheckError::message)
                                .collect(Collectors.joining("\n"));
        telegramClient.execute(
                SendMessage.builder()
                        .chatId(chatId)
                        .text(message)
                        .build());
    }

    @Override
    public boolean canHandle(Update update) {
        return ADVENTS_FULL_INFO.is(update) &&
                personService.isAdmin(MessageUtils.getTelegramUserId(update));
    }
}
