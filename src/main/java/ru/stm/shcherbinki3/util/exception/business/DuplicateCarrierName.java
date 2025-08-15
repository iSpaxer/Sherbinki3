package ru.stm.shcherbinki3.util.exception.business;

import org.springframework.http.HttpStatus;

public class DuplicateCarrierName extends BusinessException {
    public DuplicateCarrierName(String carrierName) {
        super("Carrier with this name already exists: " + carrierName, HttpStatus.CONFLICT);
    }
}
