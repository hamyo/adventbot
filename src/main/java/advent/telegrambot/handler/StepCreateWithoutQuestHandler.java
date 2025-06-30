package advent.telegrambot.handler;

import advent.telegrambot.domain.Step;
import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.repository.StepRepository;
import advent.telegrambot.service.AdminProgressService;
import advent.telegrambot.service.AdventService;
import advent.telegrambot.service.StepCommon;
import advent.telegrambot.service.StepService;
import advent.telegrambot.utils.AppException;
import advent.telegrambot.utils.MessageUtils;
import advent.telegrambot.utils.NumberUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class StepCreateWithoutQuestHandler implements StepCreateHandler {
    private final AdminProgressService adminProgressService;
    private final AdventService adventService;
    private final StepCommon stepCommon;
    private final StepService stepService;

    @Override
    public boolean canHandle(Integer questType) {
        return questType == null;
    }

    @Override
    public String getMessageForCreate() {
        return """
                Для добавления шага (Без задания) введите:
                день,
                порядок шага (оставьте строку пустой - порядок будет максимальный в рамках дня),
                текст без переносов строки (если он нужен).
                Каждые новые данные вводятся с новой строки. Порядок важен.
                Пример,
                1
                
                Привет, послушай новогоднюю музыку.
                """;
    }

    private Step createStep(String input, @NonNull Advent advent) {
        if (input == null) {
            throw new AppException("Нет данных шага");
        }

        String[] data = input.split("\n");
        if (data.length < 1 || data.length > 3) {
            throw new AppException("Ожидаются данные на одной, двух или трёх строчках");
        }

        Step step = new Step();
        step.setDay(NumberUtils.parseShort(data[0], "День"));
        step.setOrder(stepCommon.getStepOrder(data.length > 1 ? data[1] : null, advent, step.getDay()));
        if (data.length == 3) {
            step.setText(data[2]);
        }

        step.setAdvent(advent);
        stepCommon.check(step);
        return step;
    }

    @Override
    public Long createStep(Update update) {
        Pair<Integer, Integer> ids = adminProgressService.getAdventStepsCreateIds(MessageUtils.getTelegramUserId(update));
        Advent advent = adventService.findById(ids.getLeft());
        Step step = createStep(MessageUtils.getMessageText(update), advent);
        stepService.save(step);
        return step.getId();
    }
}
