package advent.telegrambot.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class CodeService {
    @Value("${app.codes.allowed-symbols}")
    private String allowedSymbols;
    private static final Random random = new Random();

    public @NonNull String generateCode(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(allowedSymbols.length());
            sb.append(allowedSymbols.charAt(randomIndex));
        }
        return sb.toString();
    }
}
