package advent.telegrambot.handler.advent;

import advent.telegrambot.domain.advent.AdventByMessage;
import advent.telegrambot.service.StepService;
import advent.telegrambot.utils.MessageUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Service
@RequiredArgsConstructor
public class AdventByMessageHandler implements AdventHandler<AdventByMessage> {
    private final StepService stepService;

    @Override
    public void startDay(@NotNull AdventByMessage advent, @NonNull Short day, String messageText) {
        stepService.handleStartDayStep(advent, day);
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
