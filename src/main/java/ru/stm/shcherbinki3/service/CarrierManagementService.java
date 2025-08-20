package ru.stm.shcherbinki3.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.stm.shcherbinki3.model.type.RecordStatus;

@Service
@Slf4j
@RequiredArgsConstructor
public class CarrierManagementService {
    private final UserService userService;
    private final CarrierService carrierService;

    @Transactional
    public void deleteUserAndCarrier(Long userId) {
        if (userService.hasCarrier(userId, RecordStatus.ACTIVE)) {
            carrierService.softDelete(userId);
        }
        userService.setDeleted(userId, RecordStatus.DELETED);
        log.info("Successfully deleted userId={} and associated carrier", userId);
    }

    @Transactional
    public void restoreUserAndCarrier(Long userId) {
        if (userService.hasCarrier(userId, RecordStatus.DELETED)) {
            carrierService.softRestore(userId);
        }
        userService.setDeleted(userId, RecordStatus.ACTIVE);
        log.info("Successfully restored userId={} and associated carrier", userId);
    }
}
