package ru.stm.shcherbinki3.dto.carrier;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.stm.shcherbinki3.model.type.RecordStatus;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarrierDto {

    private Long id;

    @NotBlank
    @Size(min = 2, max = 255, message = "Carrier name must be between 2 and 255 characters")
    private String name;

    @Pattern(regexp = "7\\d{10}", message = "Phone must be in format 7XXXXXXXXXX")
    private String phone;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private RecordStatus recordStatus;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CarrierDto that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "CarrierDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", recordStatus=" + recordStatus +
                '}';
    }
}
