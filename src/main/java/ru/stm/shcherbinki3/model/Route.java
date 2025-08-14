package ru.stm.shcherbinki3.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Route {

    private Long id;

    private Carrier carrier;

    private List<Ticket> tickets;

    @NotNull(message = "Departure cannot be null")
    @Size(min = 2, max = 100, message = "Departure must be between 2 and 100 characters")
    private String departure;

    @NotNull(message = "Destination cannot be null")
    @Size(min = 2, max = 100, message = "Destination must be between 2 and 100 characters")
    private String destination;

    @NotNull(message = "Duration cannot be null")
    @Positive(message = "Duration must be positive")
    private Duration durationMinutes;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Route route)) return false;
        return Objects.equals(id, route.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
