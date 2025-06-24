package advent.telegrambot.service;

import advent.telegrambot.domain.Step;
import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.domain.advent.AdventByCode;
import advent.telegrambot.domain.dto.AdventInfo;
import advent.telegrambot.domain.dto.CheckError;
import advent.telegrambot.handler.TelegramCommand;
import advent.telegrambot.repository.AdventRepository;
import advent.telegrambot.repository.StepRepository;
import advent.telegrambot.utils.AppException;
import advent.telegrambot.utils.DateUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdventService {
    private final AdventRepository adventRepository;
    private final StepRepository stepRepository;
    private final CodeService codeService;

    @Transactional
    public Integer create(Advent advent) {
        adventRepository.save(advent);
        return advent.getId();
    }

    public List<AdventInfo> getNotFinishedAdvents() {
        LocalDate maxFinishDate = LocalDate.now().minusDays(5);
        return adventRepository.findNotFinishedAdvents(maxFinishDate);
    }

    @Transactional(readOnly = true)
    public List<CheckError> check(@NonNull Integer id) {
        List<CheckError> errors = new ArrayList<>();
        Advent advent = findById(id);
        short maxDay = stepRepository.getMaxDaysByAdvent(advent);
        for (short i = 1; i <= maxDay; i++) {
            List<Step> daySteps = stepRepository.findByAdventAndDayAndOrderGreaterThanOrderByOrderAsc(advent, i, (short) 0);
            if (daySteps.isEmpty()) {
                errors.add(CheckError.warning(
                        String.format(
                                "День %s (%s) пропущен.",
                                i,
                                advent.getStartDate()
                                        .plusDays(i - 1)
                                        .format(DateUtils.getRusDateFormatter()))
                ));
            }

            boolean isQuestNotExists = true;
            Step previousStep = null;
            for (Step step : daySteps) {
                if (!step.getQuests().isEmpty()) {
                    isQuestNotExists = false;
                }

                if (previousStep != null) {
                  if (previousStep.getOrder() != step.getOrder() - 1) {
                      errors.add(CheckError.warning(
                         String.format(
                                 "День %s. Шаг (id=%s, порядок=%s) не по порядку cо следующим шагом (id=%s, порядок=%s)",
                                 i,
                                 previousStep.getId(),
                                 previousStep.getOrder(),
                                 step.getId(),
                                 step.getOrder()
                                 )
                      ));
                  }
                }

                previousStep = step;
            }

            if (isQuestNotExists) {
                errors.add(CheckError.error(
                        String.format(
                                "День %s (%s) без задания",
                                i,
                                advent.getStartDate()
                                        .plusDays(i - 1)
                                        .format(DateUtils.getRusDateFormatter()))
                ));
            }
        }

        return errors;
    }

    @Transactional
    public void setChatId(long chatId) {
        Advent advent = adventRepository.findLastByChatIdNullOrderByIdDesc();
        if (advent == null) {
            throw new AppException("Адвент с пустым id чата не найден");
        }
        advent.setChatId(chatId);
    }

    @Transactional(readOnly = true)
    public @NonNull Advent findByChatId(long chatId) {
        Advent advent = adventRepository.findAdventByChatId(chatId);
        if (advent == null) {
            throw new AppException("Для данного чата " + chatId +
                    " не найден адвент. Возможно, что нужно установить связь адвента с чатом через команду " +
                    TelegramCommand.SET_CHAT_ID.getAction());
        }

        return advent;
    }

    @Transactional(readOnly = true)
    public @NonNull Advent findById(@NonNull Integer id) {
        return adventRepository.findById(id)
                .orElseThrow(() -> new AppException("Не удалось найти адвент с id=" + id));
    }

    @Transactional(readOnly = true)
    public byte[] getCodes(@NonNull Integer adventId) {
        Advent advent = findById(adventId);
        if (advent instanceof AdventByCode adventByCode) {
            return String.join("\n", adventByCode.getCodes())
                    .getBytes(StandardCharsets.UTF_8);
        }

        throw new AppException("Для данного адвента не предусмотрено кодов");
    }

    @Transactional(readOnly = true)
    public Advent findByStepsQuestsId(Long questId) {
        return adventRepository.findByStepsQuestsId(questId)
                .orElseThrow(() -> new AppException("Не найден адвент для задания id=" + questId));
    }
}
