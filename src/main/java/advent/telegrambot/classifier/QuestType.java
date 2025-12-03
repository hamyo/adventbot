package advent.telegrambot.classifier;

import advent.telegrambot.domain.ClsQuestType;
import lombok.Getter;

@Getter
public enum QuestType {
    ADMIN_DECISION(1),
    ANY_ANSWER(2),
    ALL_PERSON_ANSWER(3),
    TEXT_ANSWER(4),
    BULLS_AND_COWS(5),
    APOJ(6),
    ;

    private final Integer id;

    QuestType(int id) {
        this.id = id;
    }

    public boolean is(Integer id) {
        return this.id.equals(id);
    }

    public boolean is(ClsQuestType type) {
        return type != null && this.is(type.getId());
    }

    public boolean isNot(ClsQuestType type) {
        return !is(type);
    }
}
