package advent.telegrambot.domain.quest;

import advent.telegrambot.classifier.QuestType;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

@Entity
@jakarta.persistence.DiscriminatorValue("1")
public class QuestWithAdminDecision extends Quest {
    @Override
    @Transient
    public QuestType getQuestType() {
        return QuestType.ADMIN_DECISION;
    }
}
