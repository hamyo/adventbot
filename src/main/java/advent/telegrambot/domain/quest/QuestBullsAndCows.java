package advent.telegrambot.domain.quest;

import advent.telegrambot.classifier.QuestType;
import advent.telegrambot.domain.ClsQuestType;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@jakarta.persistence.DiscriminatorValue("5")
public class QuestBullsAndCows extends Quest {

    @Override
    @Transient
    public QuestType getQuestType() {
        return QuestType.BULLS_AND_COWS;
    }
}
