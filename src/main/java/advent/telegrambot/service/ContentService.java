package advent.telegrambot.service;

import advent.telegrambot.domain.Content;
import advent.telegrambot.domain.Step;
import advent.telegrambot.repository.ContentRepository;
import advent.telegrambot.repository.StepRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContentService {
    private final AdminProgressService adminProgressService;
    private final StepService stepService;
    private final StepRepository stepRepository;
    private final ContentRepository contentRepository;

    @Transactional
    public Integer saveStepContent(@NonNull Long personId, @NonNull Content content) {
        Long stepId = adminProgressService.getAdventStepId(personId);
        Step step = stepService.getById(stepId);
        step.setContent(content);
        contentRepository.save(content);
        return step.getAdvent().getId();
    }
}
