package ru.stm.shcherbinki3.util.exception.business;

import org.springframework.http.HttpStatus;
import ru.stm.shcherbinki3.model.Carrier;

public class SingletonCarrierForUser extends BusinessException {
    public SingletonCarrierForUser(Long userId) {
        super(String.format(
                "User with id %d already owns a carrier. Each user can have only one carrier.",
                userId
        ), HttpStatus.CONFLICT);
    }
}
