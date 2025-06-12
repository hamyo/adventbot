package advent.telegrambot.domain.advent;

import advent.telegrambot.classifier.AdventType;
import jakarta.persistence.Transient;

@lombok.Getter
@lombok.Setter
@jakarta.persistence.Entity
@jakarta.persistence.DiscriminatorValue("1")
// BY_MESSAGE
public class AdventByMessage extends Advent {
    @Override
    @Transient
    public AdventType getAdventType() {
        return AdventType.BY_MESSAGE;
    }
}
