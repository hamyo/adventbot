package advent.telegrambot.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdventCurrentStepData {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long showedHintId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private char[] guessWord;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer numberOfAttempts;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Set<Long> alreadyAnsweredPersonIds = new HashSet<>();

    @JsonIgnore
    public @NonNull Long getNotEmptyShowedHintId() {
        return showedHintId == null ? 0L : showedHintId;
    }
}
