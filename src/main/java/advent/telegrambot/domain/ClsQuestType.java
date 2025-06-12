package advent.telegrambot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@ToString
@Table(name = "cls_quest_type")
public class ClsQuestType {
    @Id
    @Column(name = "cqt_id", nullable = false)
    private Integer id;

    @Column(name = "cqt_name", nullable = false, length = 100)
    private String name;

    @Column(name = "cqt_description", length = 300)
    private String description;

    public String getInfo() {
        return "тип: " + name;
    }

}