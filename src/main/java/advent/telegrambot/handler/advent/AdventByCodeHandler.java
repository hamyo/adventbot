package advent.telegrambot.handler.advent;

import advent.telegrambot.domain.advent.AdventByCode;
import advent.telegrambot.handler.TelegramCommand;
import advent.telegrambot.repository.StepRepository;
import advent.telegrambot.service.CodeService;
import advent.telegrambot.service.StepService;
import advent.telegrambot.utils.AppException;
import advent.telegrambot.utils.MessageUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static advent.telegrambot.handler.TelegramCommand.ADVENTS_CODES;
import static advent.telegrambot.handler.TelegramCommand.ADVENTS_PERSONS;

@Service
@RequiredArgsConstructor
public class AdventByCodeHandler implements AdventHandler<AdventByCode> {
    private final StepService stepService;
    private final StepRepository stepRepository;
    private final CodeService codeService;

    public void startDay(@NotNull AdventByCode advent, @NonNull Short day, String messageText) {
        String foundCode = advent.getCodes().stream()
                .filter(code -> StringUtils.equalsIgnoreCase(code, messageText))
                .findFirst()
                .orElseThrow(() -> new AppException("Указанный код не найден"));
        stepService.handleStartDayStep(advent, day);
        advent.getCodes().remove(foundCode);
    }

    @Override
    public InlineKeyboardMarkup getAdminKeyboard(@NotNull Integer adventId) {
        List<InlineKeyboardRow> menu = new ArrayList<>(MessageUtils.getAdminActionKeyboard(adventId));
        menu.add(new InlineKeyboardRow(
                Arrays.asList(
                        InlineKeyboardButton.builder()
                                .text(ADVENTS_CODES.getRusName())
                                .callbackData(TelegramCommand.getAdventCodeCommand(adventId))
                                .build())));
        return new InlineKeyboardMarkup(menu);
    }

    @Override
    @Transactional
    public void afterStepSave(@NotNull AdventByCode advent) {
        while (advent.getCodes().size() < stepRepository.countDistinctDaysByAdvent(advent)) {
            String code = codeService.generateCode(10);
            advent.getCodes().add(code);
        }
    }

    @Override
    public Class<AdventByCode> getHandledQuestClass() {
        return AdventByCode.class;
    }
}
