package advent.telegrambot.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "admin_progress")
@NoArgsConstructor
@AllArgsConstructor
public class AdminProgress {
    @Id
    @Column(name = "p_id", nullable = false)
    private Long p_id;

    @Column(name = "aps_data")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> data = new HashMap<>();
}