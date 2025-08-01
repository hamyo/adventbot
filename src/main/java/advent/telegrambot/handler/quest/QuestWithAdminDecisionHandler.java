package advent.telegrambot.handler.quest;

import advent.telegrambot.domain.Person;
import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.domain.quest.QuestWithAdminDecision;
import advent.telegrambot.repository.PersonRepository;
import advent.telegrambot.service.AdventService;
import advent.telegrambot.service.StepCommon;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static advent.telegrambot.utils.MessageUtils.getTelegramUserId;


@Component
@RequiredArgsConstructor
public class QuestWithAdminDecisionHandler implements QuestHandler<QuestWithAdminDecision> {
    private final StepCommon stepCommon;
    private final TelegramClient telegramClient;
    private final PersonRepository personRepository;
    private final AdventService adventService;

    @Override
    public void handle(@NotNull QuestWithAdminDecision quest, Update update) {
        long userId = getTelegramUserId(update);
        Advent advent = adventService.findByStepsQuestsId(quest.getId());
        personRepository.findById(userId)
                .map(Person::getIsAdmin)
                .ifPresentOrElse(person -> {
                    stepCommon.handleNextSteps(advent);
                }, () -> {
                    try {
                        telegramClient.execute(
                                SendMessage.builder()
                                        .chatId(advent.getChatId())
                                        .text("Нужно одобрение администратора")
                                        .build()
                        );
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Override
    public Class<QuestWithAdminDecision> getHandledQuestClass() {
        return QuestWithAdminDecision.class;
    }
}
