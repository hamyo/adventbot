package advent.telegrambot.service;

import advent.telegrambot.domain.AdminProgress;
import advent.telegrambot.handler.TelegramCommand;
import advent.telegrambot.repository.AdminProgressRepository;
import advent.telegrambot.utils.AppException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static advent.telegrambot.handler.TelegramCommand.*;

@Service
@RequiredArgsConstructor
public class AdminProgressService {
    private final AdminProgressRepository adminProgressRepository;

    private static final String COMMAND_FIELD = "command";
    private static final String STEP_ID_FIELD = "step_id";
    private static final String QUEST_TYPE_FIELD = "quest_type";
    private static final String ADVENT_ID_FIELD = "advent_id";

    @Transactional
    public void save(long personId, TelegramCommand command) {
        AdminProgress adminProgress = new AdminProgress(
                personId,
                Map.of(COMMAND_FIELD, command.name()));
        adminProgressRepository.save(adminProgress);
    }

    @Transactional
    public void saveAdventId(long personId, @NonNull Integer id) {
        AdminProgress adminProgress = new AdminProgress(
                personId,
                Map.of(
                        COMMAND_FIELD, ADVENTS_PERSONS.name(),
                        ADVENT_ID_FIELD, id.toString()));
        adminProgressRepository.save(adminProgress);
    }

    public void saveStepId(long personId, @NonNull Long id) {
        AdminProgress adminProgress = new AdminProgress(
                personId,
                Map.of(COMMAND_FIELD, ADVENTS_STEPS_CREATED.name(),
                        STEP_ID_FIELD, id.toString()));
        adminProgressRepository.save(adminProgress);
    }

    @Transactional(readOnly = true)
    public @NonNull Long getAdventStepId(long personId) {
        return adminProgressRepository.findById(personId)
                .map(AdminProgress::getData)
                .filter(data -> data.getOrDefault(COMMAND_FIELD, "").equals(ADVENTS_STEPS_CREATED.name()))
                .map(data -> Long.parseLong(data.get(STEP_ID_FIELD)))
                .orElseThrow(() -> new AppException("Не удалось найти идентификатор созданного шага"));
    }

    @Transactional(readOnly = true)
    public @NonNull Integer getAdventId(long personId) {
        return adminProgressRepository.findById(personId)
                .map(AdminProgress::getData)
                .map(data -> Integer.parseInt(data.get(ADVENT_ID_FIELD)))
                .orElseThrow(() -> new AppException("Не удалось найти идентификатор"));
    }

    @Transactional(readOnly = true)
    public @NonNull Pair<Integer, Integer> getAdventStepsCreateIds(long personId) {
        return adminProgressRepository.findById(personId)
                .map(AdminProgress::getData)
                .filter(data -> data.getOrDefault(COMMAND_FIELD, "").equals(ADVENTS_STEPS_CREATE.name()))
                .map(data -> {
                    String questType = data.get(QUEST_TYPE_FIELD);
                    return Pair.of(
                            Integer.parseInt(data.get(ADVENT_ID_FIELD)),
                            StringUtils.isBlank(questType) ? null : Integer.parseInt(data.get(QUEST_TYPE_FIELD)));
                })
                .orElseThrow(() -> new AppException("Не удалось найти нужные идентификатора для создания шага"));
    }

    @Transactional
    public void saveAdventStepsCreate(long personId, @NonNull String action) {
        Map<String, String> params = new HashMap<>();
        params.put(COMMAND_FIELD, ADVENTS_STEPS_CREATE.name());
        Pair<Integer, Integer> ids = TelegramCommand.getIdsFromStepCreateCommand(action);
        params.put(ADVENT_ID_FIELD, ids.getLeft().toString());
        if (ids.getRight() != null) {
            params.put(QUEST_TYPE_FIELD, ids.getRight().toString());
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
    public void delete(long personId) {
        adminProgressRepository.deleteById(personId);
    }
}
