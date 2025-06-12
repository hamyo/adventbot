package advent.telegrambot.domain.quest;

import advent.telegrambot.classifier.QuestType;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@jakarta.persistence.DiscriminatorValue("3")
public class QuestWithAllPersonAnswer extends QuestWithNeedTypes {
    @Override
    @Transient
    public QuestType getQuestType() {
        return QuestType.ALL_PERSON_ANSWER;
    }
}
