package advent.telegrambot.domain.quest;

import advent.telegrambot.classifier.QuestType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@jakarta.persistence.DiscriminatorValue("4")

public class QuestWithTextAnswer extends Quest {

    @Column(name = "q_right_values")
    private List<String> rightValues = new ArrayList<>();

    @Override
    @Transient
    public QuestType getQuestType() {
        return QuestType.TEXT_ANSWER;
    }

    @Override
    public String getRusInfo(String typeName) {
        return String.format(
                "Задание %s id=%s Правильные значения(%s)",
                typeName,
                getId(),
                String.join(",", getRightValues()));
    }
}


