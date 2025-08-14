package ru.stm.shcherbinki3.util.exception;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(
            @NotNull(message = "Email cannot be null")
            @Email(message = "Email must be a valid email address")
            @Size(max = 255, message = "Email must be at most 255 characters")
            String email) {
        super(email);
    }
}
