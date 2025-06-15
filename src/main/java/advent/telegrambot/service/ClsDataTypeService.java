package advent.telegrambot.service;

import advent.telegrambot.repository.ClsDataTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClsDataTypeService {
    private final ClsDataTypeRepository clsDataTypeRepository;

    public String getAllDataTypeDescription() {
        return clsDataTypeRepository.findAll().stream()
                .map(dataType -> String.format("%s (%s)", dataType.getId(), dataType.getName()))
                .collect(Collectors.joining(",", "(", ")"));
    }
}
