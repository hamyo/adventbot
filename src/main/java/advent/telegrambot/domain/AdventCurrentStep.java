package advent.telegrambot.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "advent_current_step")
@NoArgsConstructor
public class AdventCurrentStep {
    public AdventCurrentStep(@NonNull Integer id, @NonNull Step step) {
        this.id = id;
        this.step = step;
    }

    @Id
    @Column(name = "a_id", nullable = false)
    private Integer id;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "s_id")
    private Step step;

    @Column(name = "acs_data")
    @JdbcTypeCode(SqlTypes.JSON)
    private AdventCurrentStepData data = new AdventCurrentStepData();

    public AdventCurrentStepData getData() {
        if (data == null) {
            data = new AdventCurrentStepData();
        }
        return data;
    }
}