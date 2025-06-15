package advent.telegrambot.service;

import advent.telegrambot.domain.ClsQuestType;
import advent.telegrambot.repository.ClsQuestTypeRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClsQuestTypeService {
    private final ClsQuestTypeRepository clsQuestTypeRepository;

    public @NonNull String getQuestTypeName(@NonNull Integer id) {
        return clsQuestTypeRepository.findById(id).map(ClsQuestType::getName).orElse("");
    }
}
