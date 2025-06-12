package advent.telegrambot.service;

import advent.telegrambot.domain.AdventCurrentStep;
import advent.telegrambot.repository.AdventCurrentStepRepository;
import advent.telegrambot.utils.AppException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdventCurrentStepService {
    private final AdventCurrentStepRepository adventCurrentStepRepository;

    @Transactional(readOnly = true)
    public @NonNull AdventCurrentStep findById(@NonNull Integer id) {
        return adventCurrentStepRepository.findById(id)
                .orElseThrow(() -> new AppException("Для адвента " + id + " не найдено текущего прогресса"));
    }

    @Transactional
    public void save(@NonNull AdventCurrentStep adventCurrentStep) {
        adventCurrentStepRepository.save(adventCurrentStep);
    }
}
