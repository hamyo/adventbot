package advent.telegrambot;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
public class Config {
    @Value("${telegram.bot.token}") private String token;

    @Bean("telegramClient")
    public TelegramClient getTelegramClient() {
        return new OkHttpTelegramClient(token);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // Customize ObjectMapper settings here
        return objectMapper;
    }
}
