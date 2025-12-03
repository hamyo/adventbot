package advent.telegrambot.domain.quest;

import advent.telegrambot.classifier.QuestType;
import advent.telegrambot.domain.Hint;
import advent.telegrambot.domain.QuestContent;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@jakarta.persistence.DiscriminatorValue("6")
public class QuestApoj extends Quest {
    @Column(name = "q_second_duration")
    private Short secondDuration;

    @Column(name = "q_part_count")
    private Short partCount;

    @OneToMany(mappedBy = "quest", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    @OrderBy("order ASC")
    private List<QuestContent> questContents = new ArrayList<>();


    @Override
    @Transient
    public QuestType getQuestType() {
        return QuestType.APOJ;
    }
}
