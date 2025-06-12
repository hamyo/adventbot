package advent.telegrambot.handler.admin;

import advent.telegrambot.domain.Person;
import advent.telegrambot.handler.MessageHandler;
import advent.telegrambot.handler.TelegramCommand;
import advent.telegrambot.repository.PersonRepository;
import advent.telegrambot.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
@RequiredArgsConstructor
public class AdminAddHandler implements MessageHandler {
    private final PersonRepository personRepository;
    private final TelegramClient telegramClient;

    @SneakyThrows
    @Override
    @Transactional
    public void handle(Update update) {
        long personId = MessageUtils.getTelegramUserId(update.getMessage());
        Person person = new Person();
        person.setId(personId);
        person.setIsAdmin(true);
        person.setNameNominative(MessageUtils.tryGetUserName(update.getMessage()));
        personRepository.save(person);
        telegramClient.executeAsync(
                SendMessage.builder()
                        .chatId(MessageUtils.getChatId(update))
                        .text("Администратор " + person.getNameNominative() + "(id=" + person.getId() + ") успешно добавлен")
                        .build());
    }

    @Override
    public boolean canHandle(Update update) {
        return TelegramCommand.ADMIN_ADD.is(update) && !personRepository.existsAny();
    }
}
