package ru.stm.shcherbinki3.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Carrier {

    private Long id;

    private User owner; // oneToOne

    private List<User> admins; // ManyToMany отдельная таблица

    @Size(min = 2, max = 255, message = "Carrier name must be between 2 and 255 characters")
    private String name;

    @Size(min = 11, max = 11, message = "Carrier phone must be exactly 11 characters")
    private String phone;

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