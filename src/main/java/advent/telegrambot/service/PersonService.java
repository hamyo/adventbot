package advent.telegrambot.service;

import advent.telegrambot.domain.Person;
import advent.telegrambot.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PersonService {
    private final PersonRepository personRepository;

    @Transactional
    public boolean isAdmin(long id) {
        return personRepository.findById(id).map(Person::getIsAdmin).orElse(false);
    }

    public boolean isExist(long id) {
        return personRepository.existsById(id);
    }

}
