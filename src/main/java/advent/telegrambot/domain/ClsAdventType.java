package advent.telegrambot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cls_advent_type")
public class ClsAdventType {
    @Id
    @Column(name = "cat_id", nullable = false)
    private Short id;

    @Column(name = "cat_name", nullable = false, length = 50)
    private String catName;

}