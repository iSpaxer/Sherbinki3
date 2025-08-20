package ru.stm.shcherbinki3.util.exception;

//import org.springframework.security.core.AuthenticationException;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String accessIsDenied) {
        super(accessIsDenied);
    }
}
