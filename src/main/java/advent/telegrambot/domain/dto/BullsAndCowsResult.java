package advent.telegrambot.domain.dto;

import java.util.Map;

public record BullsAndCowsResult(int bullsCount, int cowsCount, int attemptCount, boolean isFinished) {
    public static BullsAndCowsResult finished(int bullsCount, int cowsCount, int attemptCount) {
        return new BullsAndCowsResult(bullsCount, cowsCount, attemptCount, true);
    }

    public static BullsAndCowsResult notFinished(int bullsCount, int cowsCount, int attemptCount) {
        return new BullsAndCowsResult(bullsCount, cowsCount, attemptCount, false);
    }
}
