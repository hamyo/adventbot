package advent.telegrambot.handler.admin;

import advent.telegrambot.FileHelper;
import advent.telegrambot.domain.Content;
import advent.telegrambot.domain.Step;
import advent.telegrambot.handler.MessageHandler;
import advent.telegrambot.handler.TelegramCommand;
import advent.telegrambot.repository.ClsQuestTypeRepository;
import advent.telegrambot.repository.StepRepository;
import advent.telegrambot.service.AdminProgressService;
import advent.telegrambot.service.PersonService;
import advent.telegrambot.service.StepService;
import advent.telegrambot.utils.AppException;
import advent.telegrambot.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@RequiredArgsConstructor
public class ContentUploadHandler implements MessageHandler {
    private final AdminProgressService adminProgressService;
    private final PersonService personService;
    private final TelegramClient telegramClient;
    private final StepService stepService;
    private final FileHelper fileHelper;
    private final ClsQuestTypeRepository clsQuestTypeRepository;
    private final StepRepository stepRepository;

    @Override
    public void handle(Update update) {
        long personId = MessageUtils.getTelegramUserId(update);
        fileHelper.getContent(update).ifPresentOrElse(
                (content) -> {
                    Long stepId = adminProgressService.getAdventStepId(personId);
                    Step step = stepService.getById(stepId);
                    step.setContent(content);
                    stepRepository.save(step);
                    SendMessage response = SendMessage.builder()
                            .chatId(MessageUtils.getChatId(update))
                            .text("Контент успешно добавлен к шагу")
                            .replyMarkup(
                                    MessageUtils.getStepActionKeyboard(
                                            step.getAdvent().getId(),
                                            clsQuestTypeRepository.findAll()))
                            .build();
                    try {
                        telegramClient.executeAsync(response);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                },
                () -> {
                    throw new AppException("Контент не удалось получить");
                }
        );
    }

    @Override
    public boolean canHandle(Update update) {
        long personId = MessageUtils.getTelegramUserId(update);
        return adminProgressService.isCurrentCommand(personId, TelegramCommand.ADVENTS_STEPS_CREATED) &&
                StringUtils.isNotBlank(fileHelper.getFileId(update)) &&
                personService.isAdmin(personId);
    }
}
