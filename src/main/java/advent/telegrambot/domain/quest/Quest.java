package advent.telegrambot.domain.quest;

import advent.telegrambot.classifier.QuestType;
import advent.telegrambot.domain.ClsQuestType;
import advent.telegrambot.domain.Hint;
import advent.telegrambot.domain.Step;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "quest")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "cqt_id", discriminatorType = DiscriminatorType.INTEGER)
public abstract class Quest {
    @Id
    @Column(name = "q_id", nullable = false)
    private Long id;

    @OneToMany(mappedBy = "quest", fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    @OrderBy("id ASC")
    private List<Hint> hints = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "s_id")
    private Step step;

    public String getRusInfo(String typeName) {
        return String.format("Задание %s id=%s", typeName, id);
    }

    public abstract QuestType getQuestType();
}