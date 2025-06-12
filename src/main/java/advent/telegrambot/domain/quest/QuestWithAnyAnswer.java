package advent.telegrambot.domain.quest;

import advent.telegrambot.classifier.QuestType;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

@Entity
@jakarta.persistence.DiscriminatorValue("2")
public class QuestWithAnyAnswer extends QuestWithNeedTypes {
    @Override
    @Transient
    public QuestType getQuestType() {
        return QuestType.ANY_ANSWER;
    }
}
