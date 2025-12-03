package advent.telegrambot.handler.quest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class QuestWithTextAnswerHandlerTest {
    @InjectMocks
    private QuestWithTextAnswerHandler handler;

    @Test
    public void checkIsAnswerRight() {
        List<String> expected = Collections.singletonList("привет мир.как дела?хорошо! надеюсь-да нет, точно да");
        String input = "Привет,мир!Как дела?( Хорошо.) [Надеюсь] /Да\\Нет-точно. Да";
        assertThat(handler.isAnswerRight(expected, input)).isTrue();
    }

}