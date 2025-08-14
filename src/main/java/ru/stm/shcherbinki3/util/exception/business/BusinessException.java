package ru.stm.shcherbinki3.util.exception.business;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BusinessException extends RuntimeException {

    protected final HttpStatus status;

    public BusinessException(String message, HttpStatus httpStatus) {
        super(message);
        this.status = httpStatus;
    }

}
