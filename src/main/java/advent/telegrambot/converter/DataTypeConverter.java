package advent.telegrambot.converter;

import advent.telegrambot.classifier.DataType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Converter(autoApply = true)
@Slf4j
public class DataTypeConverter implements AttributeConverter<DataType, Short> {

    @Override
    public Short convertToDatabaseColumn(DataType attribute) {
        if (attribute == null) return null;
        return attribute.getId();
    }

    @Override
    public DataType convertToEntityAttribute(Short dbData) {
        if (dbData == null) return null;
        try {
            return DataType.of(dbData);
        } catch (Exception e) {
            log.warn("Unknown enum value {} in database", dbData);
            return null; // или null, или выбросить исключение
        }
    }
}
