package ru.stm.shcherbinki3.dto.route;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.stm.shcherbinki3.dto.carrier.CarrierDto;
import ru.stm.shcherbinki3.model.type.RecordStatus;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RouteWithCarrierDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private CarrierDto carrier;

    @NotNull(message = "Departure cannot be null")
    @Size(min = 2, max = 100, message = "Departure must be between 2 and 100 characters")
    private String departure;

    @NotNull(message = "Destination cannot be null")
    @Size(min = 2, max = 100, message = "Destination must be between 2 and 100 characters")
    private String destination;

    @NotNull(message = "Duration cannot be null")
    @Positive(message = "Duration must be positive")
    private Long durationMinutes;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private RecordStatus recordStatus;

}
