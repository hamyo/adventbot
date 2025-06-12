package advent.telegrambot.repository;

import advent.telegrambot.domain.ClsDataType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClsDataTypeRepository extends JpaRepository<ClsDataType, Short> {
}
