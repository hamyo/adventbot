package advent.telegrambot.domain.dto;

import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.time.LocalDate;

public interface AdventInfo {
    Integer getId();
    @Temporal(TemporalType.DATE)  // Указываем тип даты
    LocalDate getStartDate();
    String getType();
    Long getDaysCount();
}
