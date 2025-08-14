package ru.stm.shcherbinki3.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.stm.shcherbinki3.model.type.RecordStatus;

import java.util.List;
import java.util.Objects;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;

    private List<Ticket> tickets; // oneToMany
    private Carrier carrier; // oneToOne

    @NotNull(message = "Email cannot be null")
    @Email(message = "Email must be a valid email address")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String email;

    @NotBlank(message = "Password cannot be null")
    @Size(max = 255, message = "Password must be at most 255 characters")
    private String password;

    @NotBlank(message = "Name cannot be null")
    @Size(max = 120, message = "Name must be at most 120 characters")
    private String name;

    @NotBlank(message = "Lastname cannot be null")
    @Size(max = 120, message = "Lastname must be at most 120 characters")
    private String lastname;

    @Size(max = 120, message = "Patronymic must be at most 120 charactersа по")
    private String patronymic;

    private RecordStatus recordStatus;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(id, user.id) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
}