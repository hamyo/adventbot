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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdventByCodeService {
    private final AdventService adventService;
    private final StepRepository stepRepository;
    private final CodeService codeService;

    @Transactional
    public void addRandomCode(@NonNull Integer id) {
        Advent advent = adventService.findById(id);
        if (advent instanceof AdventByCode adventByCode) {
            while (adventByCode.getCodes().size() < stepRepository.countDistinctDaysByAdvent(advent)) {
                String code = codeService.generateCode(10);
                adventByCode.getCodes().add(code);
            }
        } else {
            throw new AppException("Адвент " + id + " не того типа");
        }
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
