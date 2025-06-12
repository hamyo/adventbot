package advent.telegrambot.repository;

import advent.telegrambot.domain.AdventCurrentStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdventCurrentStepRepository extends JpaRepository<AdventCurrentStep, Integer> {
}
