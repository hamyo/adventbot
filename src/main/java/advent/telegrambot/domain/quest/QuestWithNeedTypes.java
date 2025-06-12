package advent.telegrambot.domain.quest;


import advent.telegrambot.classifier.DataType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
public abstract class QuestWithNeedTypes extends Quest {
    @Column(name = "q_allowed_answer_types")
    private Set<Short> allowedAnswerTypes = new HashSet<>();

    @Transient
    public String getRusNameNeedTypes() {
        return allowedAnswerTypes.stream()
                .map(DataType::of)
                .map(DataType::getRusName)
                .collect(Collectors.joining(","));
    }

    public boolean isNeedType(DataType type) {
        return type != null && allowedAnswerTypes.contains(type.getId());
    }

    public boolean isNotNeedType(DataType type) {
        return !isNeedType(type);
    }

    @Override
    public String getRusInfo(String typeName) {
        return String.format(
                "Задание %s id=%s Разрешенные типы(%s)",
                typeName,
                getId(),
                getRusNameNeedTypes());
    }
}
