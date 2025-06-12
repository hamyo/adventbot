package advent.telegrambot.repository;

import advent.telegrambot.domain.AdminProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminProgressRepository extends JpaRepository<AdminProgress, Long> {
}
