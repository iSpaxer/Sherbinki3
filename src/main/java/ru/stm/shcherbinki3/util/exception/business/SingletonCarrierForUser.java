package ru.stm.shcherbinki3.util.exception.business;

import org.springframework.http.HttpStatus;
import ru.stm.shcherbinki3.model.Carrier;

public class SingletonCarrierForUser extends BusinessException {
    public SingletonCarrierForUser(Carrier carrier) {
        super(String.format(
                "User with id %d already owns a carrier (id: %d, name: '%s'). Each user can have only one carrier.",
                carrier.getOwner().getId(),
                carrier.getId(),
                carrier.getName()
        ), HttpStatus.CONFLICT);
    }
}
