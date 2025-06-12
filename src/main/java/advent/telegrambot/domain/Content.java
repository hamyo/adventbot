package advent.telegrambot.domain;

import advent.telegrambot.classifier.DataType;
import advent.telegrambot.converter.DataTypeConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Supplier;

@Getter
@Setter
@Entity
@Table(name = "content")
@NoArgsConstructor
public class Content {
    @Id
    @Column(name = "cn_id", nullable = false)
    private Long id;

    @Column(name = "cdt_id", nullable = false)
    @Convert(converter = DataTypeConverter.class)
    private DataType type;

    @Column(name = "cn_name", length = 300)
    private String name;

    @Column(name = "cn_data", nullable = false)
    private byte[] data;

    @Column(name = "cn_caption", length = Integer.MAX_VALUE)
    private String caption;

    public Content(DataType type, String name, String caption, byte[] data) {
        this.type = type;
        this.name = name;
        this.data = data;
        this.caption = caption;
    }

    @Transient
    public String getNotEmptyNameWithId() {
        return getId() + "_" + getNotEmptyName();
    }

    @Transient
    public String getNotEmptyName() {
        return StringUtils.isNotBlank(name) ? name : type.getDefaultName();
    }

    @Transient
    public String getRusInfo() {
        return String.format("%s id=%s (%s)\n%s",
                id,
                type.getRusName(),
                getNotEmptyNameWithId(),
                caption);
    }
}