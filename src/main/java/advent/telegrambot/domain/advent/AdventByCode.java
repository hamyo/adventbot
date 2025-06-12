package advent.telegrambot.domain.advent;

import advent.telegrambot.classifier.AdventType;
import jakarta.persistence.*;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.HashSet;
import java.util.Set;

@lombok.Getter
@lombok.Setter
@jakarta.persistence.Entity
@jakarta.persistence.DiscriminatorValue("2") //AdventType.BY_CODE
public class AdventByCode extends Advent {
    @ElementCollection(fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    @CollectionTable(
            name = "code",
            joinColumns = @JoinColumn(name = "a_id"),
            foreignKey = @ForeignKey(name = "fk_c_a")
    )
    @Column(name = "c_value", nullable = false)
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private Set<String> codes = new HashSet<>();

    @Override
    @Transient
    public AdventType getAdventType() {
        return AdventType.BY_CODE;
    }
}
