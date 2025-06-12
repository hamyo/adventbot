package advent.telegrambot.domain;

import advent.telegrambot.domain.quest.Quest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "hint")
@NoArgsConstructor
public class Hint {
    @Id
    @Column(name = "h_id", nullable = false)
    private Long id;

    @Column(name = "h_text", length = Integer.MAX_VALUE)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.RESTRICT)
    @JoinColumn(name = "cn_id")
    private Content content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "q_id", nullable = false)
    private Quest quest;

    public Hint(String text) {
        this.text = text;
    }

    @Transient
    public String getRusInfo() {
        return "Подсказка id=" + id + ". " + text;
    }
}