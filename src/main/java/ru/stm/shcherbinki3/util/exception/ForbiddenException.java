package ru.stm.shcherbinki3.util.exception;

import org.springframework.security.core.AuthenticationException;

public class ForbiddenException extends AuthenticationException {
    public ForbiddenException(String accessIsDenied) {
        super(accessIsDenied);
    }
}
