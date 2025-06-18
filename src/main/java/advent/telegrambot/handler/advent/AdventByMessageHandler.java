package advent.telegrambot.handler.advent;

import advent.telegrambot.domain.advent.AdventByMessage;
import advent.telegrambot.service.StepCommon;
import advent.telegrambot.service.StepService;
import advent.telegrambot.utils.MessageUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Component
@RequiredArgsConstructor
public class AdventByMessageHandler implements AdventHandler<AdventByMessage> {
    private final StepCommon stepCommon;

    @Override
    @Transactional
    public void startDay(@NotNull AdventByMessage advent, @NonNull Short day, String messageText) {
        stepCommon.handleStartDayStep(advent, day);
    }

    @Override
    public void afterStepSave(@NotNull AdventByMessage advent) {
    }

    @Override
    public InlineKeyboardMarkup getAdminKeyboard(@NotNull Integer adventId) {
        return new InlineKeyboardMarkup(MessageUtils.getAdminActionKeyboard(adventId));
    }

    @Override
    public Class<AdventByMessage> getHandledQuestClass() {
        return AdventByMessage.class;
    }
}
