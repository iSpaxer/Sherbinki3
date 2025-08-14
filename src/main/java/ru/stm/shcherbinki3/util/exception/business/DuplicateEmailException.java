package ru.stm.shcherbinki3.util.exception.business;

import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends BusinessException {
    public DuplicateEmailException(String email) {
        super("Email '" + email + "' is already in use. Please use a different email.",
              HttpStatus.CONFLICT);
    }
}
