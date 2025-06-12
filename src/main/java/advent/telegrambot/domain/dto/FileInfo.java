package advent.telegrambot.domain.dto;

import advent.telegrambot.classifier.DataType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class FileInfo {
    private final String id;
    private final String filename;
    private final DataType type;
    private final String mimeType;
    private final String caption;
}
