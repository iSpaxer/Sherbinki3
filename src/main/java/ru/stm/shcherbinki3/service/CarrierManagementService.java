package ru.stm.shcherbinki3.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.stm.shcherbinki3.dto.LoginRequest;
import ru.stm.shcherbinki3.model.type.RecordStatus;
import ru.stm.shcherbinki3.security.JwtUserDetails;
import ru.stm.shcherbinki3.util.exception.BadRequestException;

@Service
@Slf4j
@RequiredArgsConstructor
public class CarrierManagementService {
    private final UserService userService;
    private final CarrierService carrierService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void deleteUserAndCarrier(Long userId) {
        if (userService.hasCarrier(userId, RecordStatus.ACTIVE)) {
            carrierService.softDelete(userId);
        }
        userService.setDeleted(userId, RecordStatus.DELETED);
        log.info("Successfully deleted userId={} and associated carrier", userId);
    }

    @Transactional
    public void restoreUserAndCarrier(LoginRequest dto) {
        JwtUserDetails jwtUserDetails = userService.getUserDetailsByEmail(dto.getEmail(), RecordStatus.DELETED);
        if (passwordEncoder.matches(dto.getPassword(), jwtUserDetails.getPassword())) {
            restoreUserAndCarrier(jwtUserDetails.getId());
        } else {
            throw new BadRequestException("Invalid username or password");
        }
    }

    @Transactional
    private void restoreUserAndCarrier(Long userId) {
        if (userService.hasCarrier(userId, RecordStatus.DELETED)) {
            carrierService.softRestore(userId);
        }
        userService.setDeleted(userId, RecordStatus.ACTIVE);
        log.info("Successfully restored userId={} and associated carrier", userId);
    }

}
