package ru.stm.shcherbinki3.dto.ticket;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketPublicDto implements Serializable {

    @NotNull
    private Long id;

    @NotNull(message = "Seat number cannot be null")
    @Positive(message = "Seat number must be positive")
    private Integer placeNumber;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean isPurchased;

}
