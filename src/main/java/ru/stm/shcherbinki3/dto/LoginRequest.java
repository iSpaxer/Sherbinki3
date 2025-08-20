package ru.stm.shcherbinki3.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login request model containing email and password")
public class LoginRequest {

    @Schema(
            description = "User's email address used for authentication and communication",
            example = "a.v.sybachin@mail.ru"
    )
    @NotNull(message = "Email cannot be null")
    @Email(message = "Email must be a valid email address")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String email;

    @Schema(
            description = "User's password. Provided only during registration or when changing the password." +
                    "Must be at least 8 characters long",
            example = "Qwerty123!"
    )
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "Password cannot be null")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters long")
    private String password;

}
