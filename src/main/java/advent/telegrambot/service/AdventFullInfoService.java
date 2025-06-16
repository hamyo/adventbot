package advent.telegrambot.service;

import advent.telegrambot.domain.*;
import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.domain.quest.Quest;
import advent.telegrambot.repository.ClsQuestTypeRepository;
import advent.telegrambot.repository.StepRepository;
import advent.telegrambot.utils.ZipUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class AdventFullInfoService {
    private final AdventService adventService;
    private final StepRepository stepRepository;
    private final ClsQuestTypeRepository clsQuestTypeRepository;

    @SneakyThrows
    @Transactional(readOnly = true)
    public String getAdventPersonsInfo(@NonNull Long chatId, @NonNull Integer adventId) {
        Advent advent = adventService.findById(adventId);
        return advent.getPersons().stream()
                .map(Person::getRusString)
                .collect(Collectors.joining("\n", "Участники:\n", ""));
    }

    @SneakyThrows
    @Transactional(readOnly = true)
    public byte[] getAdventDayStepInfo(short day, @NonNull Advent advent) {
        List<Step> steps = stepRepository.findByAdventAndDayAndOrderGreaterThanOrderByOrderAsc(advent, day, (short) 0);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            StringBuilder stepInfo = new StringBuilder();
            for (Step step : steps) {
                if (!stepInfo.isEmpty()) {
                    stepInfo.append("\n");
                }
                stepInfo.append(step.getRusInfo());
                addContentInfoIfNeed(stepInfo, zos, step.getContent());

                for (Quest quest : step.getQuests()) {
                    String typeName = clsQuestTypeRepository.findById(quest.getQuestType().getId())
                            .map(ClsQuestType::getName)
                            .orElse("");
                    stepInfo.append("\n").append("\t").append(quest.getRusInfo(typeName));
                    for (Hint hint : quest.getHints()) {
                        stepInfo.append("\n").append("\t").append("\t").append(hint.getRusInfo());
                        addContentInfoIfNeed(stepInfo, zos, hint.getContent());
                    }
                }

                stepInfo.append("\n");
            }

            ZipUtils.addFileToZip(zos, "info.txt", stepInfo.toString().getBytes(StandardCharsets.UTF_8));
            zos.finish();
            return baos.toByteArray();
        }
    }

    private void addContentInfoIfNeed(@NonNull StringBuilder info, @NonNull ZipOutputStream zos, Content content) {
        if (content == null) {
            return;
        }

        info.append("\n").append(content.getRusInfo());
        ZipUtils.addFileToZip(zos, content.getNotEmptyNameWithId(), content.getData());
    }
}
