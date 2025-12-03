package advent.telegrambot.domain;

import advent.telegrambot.domain.quest.Quest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "quest_content")
@NoArgsConstructor
public class QuestContent {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cn_id")
    private Quest quest;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cn_id")
    private Content content;

    @Id
    @Column(name = "qcn_order")
    private Short order;
}
