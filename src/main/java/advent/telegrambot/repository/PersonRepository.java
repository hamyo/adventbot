package advent.telegrambot.repository;

import advent.telegrambot.domain.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    @Query(value = "SELECT EXISTS(SELECT 1 FROM person)", nativeQuery = true)
    boolean existsAny();
}
