package advent.telegrambot.repository;

import advent.telegrambot.domain.ClsQuestType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClsQuestTypeRepository extends JpaRepository<ClsQuestType, Integer> {
}
