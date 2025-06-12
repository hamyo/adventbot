package advent.telegrambot.domain.dto;

import java.time.LocalDate;

public record AdventInfo (Integer id, LocalDate startDate, String type, Short daysCount) {
}
