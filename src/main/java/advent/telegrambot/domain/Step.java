package advent.telegrambot.domain;

import advent.telegrambot.domain.advent.Advent;
import advent.telegrambot.domain.quest.Quest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "step")
@NoArgsConstructor
@NamedEntityGraphs({
        @NamedEntityGraph(
                name = "Step.withContentAndQuests",
                attributeNodes = {
                        @NamedAttributeNode("content"),
                        @NamedAttributeNode("quests")
                }
        )
})
public class Step {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "step", fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    private List<Quest> quests = new ArrayList<>();

    @Transient
    public String getRusInfo() {
        return String.format("Шаг %s (id=%s)\n%s", order, id, StringUtils.isBlank(text) ? "(Без текста)" : text);
    }

}