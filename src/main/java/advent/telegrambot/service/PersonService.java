package advent.telegrambot.service;

import advent.telegrambot.domain.Person;
import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonService {
    private final PersonRepository personRepository;
    private final AdventService adventService;

    @Transactional
    public void save(List<Person> persons, Integer adventId) {
        Advent advent = adventService.findById(adventId);
        personRepository.saveAll(persons);
        persons.forEach(person -> {
            if (advent.getPersons().stream().noneMatch(adventPerson -> adventPerson.getId().equals(person.getId()))) {
                advent.getPersons().add(person);
            }
        });
    }

    @Transactional
    public boolean isAdmin(long id) {
        return personRepository.findById(id).map(Person::getIsAdmin).orElse(false);
    }

    public boolean isExist(long id) {
        return personRepository.existsById(id);
    }

}
