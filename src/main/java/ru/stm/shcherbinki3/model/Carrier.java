package ru.stm.shcherbinki3.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.stm.shcherbinki3.model.type.RecordStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Carrier {

    private Long id;

    private User owner; // oneToOne

    private String name;

    private String phone;

    private RecordStatus recordStatus;

    private LocalDateTime deletedDatetime;

    private List<Route> routeList;

    @JsonIgnore
    public boolean expiredDatetimeForSaveCarrier() {
        return deletedDatetime != null && deletedDatetime.plusDays(30).isBefore(LocalDateTime.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Carrier carrier)) return false;
        return Objects.equals(id, carrier.id) && Objects.equals(name, carrier.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}