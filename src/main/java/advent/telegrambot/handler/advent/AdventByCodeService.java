package advent.telegrambot.handler.advent;

import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.domain.advent.AdventByCode;
import advent.telegrambot.repository.StepRepository;
import advent.telegrambot.service.AdventService;
import advent.telegrambot.service.CodeService;
import advent.telegrambot.utils.AppException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;

@Service
@RequiredArgsConstructor
public class AdventByCodeService {
    private final AdventService adventService;
    private final StepRepository stepRepository;
    private final CodeService codeService;

    @Value("app.codes.length")
    private int codeLength;

    @Transactional
    public void addRandomCode(@NonNull Integer id) {
        AdventByCode adventByCode = findById(id);
        int needCodesCount = getNeedCodesCount(adventByCode, LocalDate.now());
        while (adventByCode.getCodes().size() < needCodesCount) {
            String code = codeService.generateCode(codeLength);
            adventByCode.getCodes().add(code);
        }
    }

    private AdventByCode findById(@NonNull Integer id) {
        Advent advent = adventService.findById(id);
        if (advent instanceof AdventByCode adventByCode) {
            return adventByCode;
        } else {
            throw new AppException("Адвент " + id + " не того типа");
        }
    }

    @Transactional
    public void addCodes(@NonNull Integer id, short daysCount) {
        AdventByCode adventByCode = findById(id);
        int diff = adventByCode.getCodes().size() - daysCount;
        if (diff < 0) {
            while (diff < 0) {
                String code = codeService.generateCode(10);
                if (!adventByCode.getCodes().contains(code)) {
                    adventByCode.getCodes().add(code);
                    diff++;
                }
            }
        } else if (diff > 0) {
            Iterator<String> iterator = adventByCode.getCodes().iterator();
            while (iterator.hasNext() && diff > 0) {
                iterator.next();
                iterator.remove();
                diff--;
            }
        }
    }

    int getNeedCodesCount(@NonNull AdventByCode adventByCode, @NonNull LocalDate currentDate) {
        int days = Math.toIntExact(currentDate.isAfter(adventByCode.getStartDate()) ?
                        stepRepository.countDistinctDaysByAdvent(adventByCode) -
                ChronoUnit.DAYS.between(
                        adventByCode.getStartDate(),
                        currentDate) :
                stepRepository.countDistinctDaysByAdvent(adventByCode));
        return Math.max(days, 0);

    }

    @Transactional(readOnly = true)
    public String checkCode(@NonNull Integer id, String code) {
        if (StringUtils.isBlank(code)) {
            throw new AppException("Код не должен быть пустым");
        }

        Advent advent = adventService.findById(id);
        if (advent instanceof AdventByCode adventByCode) {
            return adventByCode.getCodes().stream()
                    .filter(existCode -> StringUtils.equalsIgnoreCase(existCode, code))
                    .findFirst()
                    .orElseThrow(() -> new AppException("Указанный код не найден"));
        } else {
            throw new AppException("Адвент " + id + " не того типа");
        }
    }

    @Transactional
    public void deleteCode(@NonNull Integer id, @NonNull String code) {
        Advent advent = adventService.findById(id);
        if (advent instanceof AdventByCode adventByCode) {
            adventByCode.getCodes().remove(code);
        } else {
            throw new AppException("Адвент " + id + " не того типа");
        }
    }
}
