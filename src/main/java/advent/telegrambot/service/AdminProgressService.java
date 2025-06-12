package advent.telegrambot.service;

import advent.telegrambot.domain.AdminProgress;
import advent.telegrambot.handler.TelegramCommand;
import advent.telegrambot.repository.AdminProgressRepository;
import advent.telegrambot.utils.AppException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static advent.telegrambot.handler.TelegramCommand.ADVENTS_STEPS_CREATE;
import static advent.telegrambot.handler.TelegramCommand.ADVENTS_STEPS_CREATED;

@Service
@RequiredArgsConstructor
public class AdminProgressService {
    private final AdminProgressRepository adminProgressRepository;

    private static final String COMMAND_FIELD = "command";
    private static final String OBJECT_ID_FIELD = "object_id";
    private static final String QUEST_TYPE_FIELD = "quest_type";

    @Transactional
    public void save(long personId, TelegramCommand command) {
        save(personId, command, null);
    }

    @Transactional
    public void save(long personId, @NonNull TelegramCommand command, Number id) {
        Map<String, Object> params = new HashMap<>();
        params.put(COMMAND_FIELD, command.name());
        if (id != null) {
            params.put(OBJECT_ID_FIELD, id);
        }

        AdminProgress adminProgress = new AdminProgress(personId, params);
        adminProgressRepository.save(adminProgress);
    }

    @Transactional(readOnly = true)
    public @NonNull Long getAdventStepId(long personId) {
        return adminProgressRepository.findById(personId)
                .map(AdminProgress::getData)
                .filter(data -> data.getOrDefault(COMMAND_FIELD, "").equals(ADVENTS_STEPS_CREATED))
                .map(data -> (Long) data.get(OBJECT_ID_FIELD))
                .orElseThrow(() -> new AppException("Не удалось найти идентификатор созданного шага"));
    }

    @Transactional(readOnly = true)
    public @NonNull Pair<Integer, Integer> getAdventStepsCreateIds(long personId) {
        return adminProgressRepository.findById(personId)
                .map(AdminProgress::getData)
                .filter(data -> data.getOrDefault(COMMAND_FIELD, "").equals(ADVENTS_STEPS_CREATE))
                .map(data -> Pair.of(
                        (Integer) data.get(OBJECT_ID_FIELD),
                        (Integer) data.get(QUEST_TYPE_FIELD)))
                .orElseThrow(() -> new AppException("Не удалось найти нужные идентификатора для создания шага"));
    }

    @Transactional
    public void saveAdventStepsCreate(long personId, @NonNull String action) {
        Map<String, Object> params = new HashMap<>();
        params.put(COMMAND_FIELD, ADVENTS_STEPS_CREATE.name());
        Pair<Integer, Integer> ids = TelegramCommand.getIdsFromStepCreateCommand(action);
        params.put(OBJECT_ID_FIELD, ids.getLeft());
        if (ids.getRight() != null) {
            params.put(QUEST_TYPE_FIELD, ids.getRight());
        }


        AdminProgress adminProgress = new AdminProgress(personId, params);
        adminProgressRepository.save(adminProgress);
    }

    @Transactional(readOnly = true)
    public boolean isCurrentCommand(long personId, TelegramCommand command) {
        return adminProgressRepository.findById(personId)
                .map(AdminProgress::getData)
                .filter(data -> data.getOrDefault(COMMAND_FIELD, "").equals(command.name()))
                .isPresent();
    }

    @Transactional
    public boolean isCurrentCommandWithId(long personId, TelegramCommand command) {
        return adminProgressRepository.findById(personId)
                .map(AdminProgress::getData)
                .filter(data -> data.getOrDefault(COMMAND_FIELD, "").equals(command.name()) &&
                        data.containsKey(OBJECT_ID_FIELD))
                .isPresent();
    }

    @Transactional
    public void delete(long personId) {
        adminProgressRepository.deleteById(personId);
    }
}
