package advent.telegrambot.domain;

import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.domain.quest.Quest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "step")
@NoArgsConstructor
public class Step {
    @Id
    @Column(name = "s_id", nullable = false)
    private java.lang.Long id;

    @Column(name = "s_order", nullable = false)
    private Short order;

    @Column(name = "s_day", nullable = false)
    private Short day;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "cn_id")
    private Content content;

    @Column(name = "s_text", length = Integer.MAX_VALUE)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "a_id", nullable = false)
    private Advent advent;

    @OneToMany(mappedBy = "step")
    private List<Quest> quests = new ArrayList<>();

    @Transient
    public String getRusInfo() {
        return String.format("Шаг %s (id=%s)\n%s", order, id, text);
    }

}