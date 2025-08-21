package ru.stm.shcherbinki3.dto.ticket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketPurchasedDto implements Serializable {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer placeNumber;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal price;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime departureDatetime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TicketPurchasedDto ticket)) return false;
        return Objects.equals(id, ticket.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
