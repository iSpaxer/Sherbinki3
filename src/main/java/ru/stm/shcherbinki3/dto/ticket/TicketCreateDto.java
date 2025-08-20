package ru.stm.shcherbinki3.dto.ticket;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.stm.shcherbinki3.util.validition.ValidCorrectionTicketsLimit;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ValidCorrectionTicketsLimit
public class TicketCreateDto {

    @Min(1)
    private Integer quantityOfPlaces;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal commonPrice;

    private LocalDateTime departureDatetime;

    private Set<CorrectionTickets> correctionTickets;

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CorrectionTickets {

        @Min(1)
        @NotNull
        private Integer place;

        @NotNull(message = "Price cannot be null")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        private BigDecimal price;

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof CorrectionTickets that)) return false;
            return Objects.equals(place, that.place);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(place);
        }
    }
}
