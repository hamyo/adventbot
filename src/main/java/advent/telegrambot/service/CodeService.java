package advent.telegrambot.service;

import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.domain.advent.AdventByCode;
import advent.telegrambot.utils.AppException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CodeService {
    private static final String ALLOWED_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random random = new Random();
    private final AdventService adventService;

    public @NonNull String generateCode(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(ALLOWED_CHARS.length());
            sb.append(ALLOWED_CHARS.charAt(randomIndex));
        }
        return sb.toString();
    }

    public byte[] getCodes(@NonNull Integer adventId) {
        Advent advent = adventService.findById(adventId);
        if (advent instanceof AdventByCode adventByCode) {
            return String.join("\n", adventByCode.getCodes())
                    .getBytes(StandardCharsets.UTF_8);
        }

        throw new AppException("Для данного адвента не предусмотрено кодов");
    }
}
