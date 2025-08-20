package ru.stm.shcherbinki3.util.exception.business;

import org.springframework.http.HttpStatus;

public class EmailUsedByDeletedUserException extends BusinessException {

    public EmailUsedByDeletedUserException(String email) {
        super("Email %s was used by deleted account".formatted(email), HttpStatus.CONFLICT);
    }

}
