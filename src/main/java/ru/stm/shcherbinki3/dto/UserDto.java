package ru.stm.shcherbinki3.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.stm.shcherbinki3.model.User;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotNull(message = "Email cannot be null")
    @Email(message = "Email must be a valid email address")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String email;

    @NotNull(message = "Password cannot be null")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters long")
    private String password;

    @NotNull(message = "Name cannot be null")
    @Size(min = 2, max = 120, message = "Name must be between 2 and 120 characters long")
    private String name;

    @NotNull(message = "Lastname cannot be null")
    @Size(min = 2, max = 120, message = "Lastname must be between 2 and 120 characters long")
    private String lastname;

    @Size(min = 2, max = 120, message = "Patronymic must be between 2 and 120 characters long")
    private String patronymic;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(id, user.getId()) && Objects.equals(email, user.getEmail());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

}
