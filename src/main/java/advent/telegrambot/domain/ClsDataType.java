package advent.telegrambot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cls_data_type")
public class ClsDataType {
    @Id
    @Column(name = "cdt_id", nullable = false)
    private Short id;

    @Column(name = "cdt_name", nullable = false, length = 30)
    private String name;

    @Column(name = "cdt_default_mime_type", length = 30)
    private String defaultMimeType;

}