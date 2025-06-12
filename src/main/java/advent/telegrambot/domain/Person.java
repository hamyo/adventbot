package advent.telegrambot.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "person")
@NoArgsConstructor
public class Person {
    public Person(Long id, String nameNominative) {
        this.id = id;
        this.nameNominative = nameNominative;
    }

    @Id
    @Column(name = "p_id", nullable = false)
    private Long id;

    @Column(name = "p_is_admin", nullable = false)
    private Boolean isAdmin = false;

    @Column(name = "p_name_nominative", length = 50)
    private String nameNominative;

    @Column(name = "p_name_genitive", length = 50)
    private String nameGenitive;

    @Transient
    public boolean isNotAdmin() {
        return !isAdmin;
    }

    @Transient
    public String getRusString() {
        return "id=" + id + ", Имя=" + nameNominative;
    }

}