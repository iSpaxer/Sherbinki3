package ru.stm.shcherbinki3.util.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmailUsedByDeletedUserException extends RuntimeException {

    String email;

    public EmailUsedByDeletedUserException(String message, String email) {
        super(message);
        this.email = email;
    }
}
