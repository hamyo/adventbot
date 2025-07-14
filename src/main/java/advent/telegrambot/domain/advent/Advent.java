package advent.telegrambot.domain.advent;

import advent.telegrambot.classifier.AdventType;
import advent.telegrambot.domain.ClsAdventType;
import advent.telegrambot.domain.Person;
import advent.telegrambot.domain.Step;
import advent.telegrambot.utils.StringUtils;
import jakarta.persistence.*;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "advent")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "cat_id", discriminatorType = DiscriminatorType.INTEGER)
public abstract class Advent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "a_id", nullable = false)
    private Integer id;

    @Column(name = "a_hello_message", nullable = false, length = Integer.MAX_VALUE)
    private String helloMessage;

    @Column(name = "a_chat_id")
    private Long chatId;

    @Column(name = "a_start_date", nullable = false)
    private LocalDate startDate;

    @ManyToMany(fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    @JoinTable(
            name = "advent_person",
            joinColumns = @JoinColumn(name = "a_id"),
            inverseJoinColumns = @JoinColumn(name = "p_id")
    )
    private java.util.Set<Person> persons = new java.util.HashSet<>();

    @OneToMany(mappedBy = "advent", fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    @OrderBy("day asc, order asc")
    private List<Step> steps = new ArrayList<>();

    @Column(name = "a_finish_date")
    private LocalDate finishDate;

    public abstract AdventType getAdventType();

    public String getInfo() {
        return String.format(
                "id %s, id чата телеграмма для адвента %s\nПриветственное сообщение:\n%s",
                id,
                StringUtils.getValueOrNotDefineMessage(chatId),
                StringUtils.getValueOrNotDefineMessage(helloMessage)
                );
    }
}