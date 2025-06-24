package advent.telegrambot.handler.admin.advent;

import advent.telegrambot.domain.Person;
import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.handler.MessageHandler;
import advent.telegrambot.handler.TelegramCommand;
import advent.telegrambot.handler.advent.AdventHandlerFactory;
import advent.telegrambot.repository.PersonRepository;
import advent.telegrambot.service.AdminProgressService;
import advent.telegrambot.service.AdventService;
import advent.telegrambot.service.PersonService;
import advent.telegrambot.utils.AppException;
import advent.telegrambot.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Arrays;
import java.util.List;

import static advent.telegrambot.handler.TelegramCommand.ADVENTS_PERSONS;
import static advent.telegrambot.utils.MessageUtils.getTelegramUserId;

@Component
@RequiredArgsConstructor
public class AdventPersonsPostHandler implements MessageHandler {
    private final PersonService personService;
    private final AdminProgressService adminProgressService;
    private final TelegramClient telegramClient;
    private final AdventService adventService;
    private final PersonRepository personRepository;
    private final AdventHandlerFactory adventHandlerFactory;

    private List<Person> createPersons(String input) {
        if (StringUtils.isEmpty(input)) {
            throw new AppException("Данные для участников не введены");
        }

        return Arrays.stream(input.split("\n"))
                .filter(StringUtils::isNotBlank)
                .map(data -> {
                    String[] personData = data.split("\\s+", 2);
                    if (personData.length != 2) {
                        throw new AppException("Для участника должны быть введены id и имя в именительном падеже. Сейчас " +
                                data);
                    }

                    long personId;
                    try {
                        personId = Long.parseLong(personData[0].trim());
                    } catch (Exception ex) {
                        throw new AppException("Для участника должен быть указан его числовой id. Сейчас " + personData[0]);
                    }

                    return personRepository.findById(personId)
                            .map(person -> {
                                person.setNameNominative(personData[1].trim());
                                return person;
                            })
                            .orElseGet(() -> {
                                Person person = new Person(personId, personData[1].trim());
                                personRepository.save(person);
                                return person;
                            });
                })
                .toList();
    }

    @SneakyThrows
    @Override
    public void handle(Update update) {
        long personId = getTelegramUserId(update);
        Integer adventId = adminProgressService.getAdventId(personId);
        List<Person> persons = createPersons(update.getMessage().getText());
        personService.save(persons, adventId);
        adminProgressService.delete(personId);
        SendMessage response = SendMessage.builder()
                .chatId(MessageUtils.getChatId(update))
                .text("Участники (количество " + persons.size() + ") успешно добавлены или обновлены")
                .replyMarkup(adventHandlerFactory.getAdminKeyboard(adventService.findById(adventId)))
                .build();
        telegramClient.execute(response);
    }

    @Override
    public boolean canHandle(Update update) {
        long personId = MessageUtils.getTelegramUserId(update);
        return adminProgressService.isCurrentCommand(personId, ADVENTS_PERSONS) &&
                TelegramCommand.isNotAnyCommand(update) &&
                personService.isAdmin(MessageUtils.getTelegramUserId(update));
    }
}
