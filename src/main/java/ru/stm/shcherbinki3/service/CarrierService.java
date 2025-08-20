package ru.stm.shcherbinki3.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.stm.shcherbinki3.dao.CarrierDao;
import ru.stm.shcherbinki3.dto.carrier.CarrierCreateDto;
import ru.stm.shcherbinki3.dto.carrier.CarrierWithRoutesDto;
import ru.stm.shcherbinki3.model.Carrier;
import ru.stm.shcherbinki3.model.type.RecordStatus;
import ru.stm.shcherbinki3.util.exception.BadRequestException;
import ru.stm.shcherbinki3.util.exception.ResourceNotFoundException;
import ru.stm.shcherbinki3.util.exception.business.DuplicateCarrierName;
import ru.stm.shcherbinki3.util.exception.business.SingletonCarrierForUser;
import ru.stm.shcherbinki3.util.mapper.CarrierMapper;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CarrierService {

    private final CarrierDao carrierDao;
    private final UserService userService;
    private final CarrierMapper mapper;

    @Transactional
    public String create(CarrierCreateDto carrierDto, Long userId) {
        Optional<Carrier> existing = carrierDao.findByName(carrierDto.getName());
        if (existing.isPresent()) {
            Carrier carrier = existing.get();
            if (carrier.getOwner().getId().equals(userId)) {
                if (carrier.getRecordStatus() == RecordStatus.DELETED) {
                    if (!carrierDao.hardDelete(userId)) {
                        log.error("Failed to create carrier: could not delete previous carrier for userId={}", userId);
                        throw new BadRequestException("Failed to delete previous carrier for user with ID " + userId);
                    }
                } else {
                    log.error("Failed to create carrier: userId={} already has an active carrier", userId);
                    throw new SingletonCarrierForUser(userId);
                }
            } else if (!carrier.expiredDatetimeForSaveCarrier()) {
                log.error("Failed to create carrier: name={} is already taken", carrierDto.getName());
                throw new DuplicateCarrierName(carrierDto.getName());
            } else if (!carrierDao.hardDelete(carrier.getOwner().getId())) {
                log.error("Failed to create carrier: could not delete expired carrier with name={}", carrierDto.getName());
                throw new BadRequestException("Failed to delete expired carrier with name " + carrierDto.getName());
            }
        }
        if (userService.hasCarrier(userId, RecordStatus.ACTIVE)) {
            log.error("Failed to create carrier: userId={} already has an active carrier", userId);
            throw new SingletonCarrierForUser(userId);
        }
        Carrier carrier = carrierDao.create(mapper.toEntity(carrierDto), userId);
        log.info("Successfully created carrier with name={} for userId={}", carrier.getName(), userId);
        return carrier.getName();
    }

    @Transactional(readOnly = true)
    public CarrierWithRoutesDto getByUserId(Long userId) {
        Carrier carrier = carrierDao.findWithRoutesByUserIdAndRecordStatus(userId, RecordStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.error("Failed to get carrier: carrier not found for userId={}", userId);
                    return new ResourceNotFoundException("Carrier for user with ID " + userId + " not found");
                });
        log.info("Successfully retrieved carrier with name={} for userId={}", carrier.getName(), userId);
        return mapper.toDtoWithListRoutes(carrier);
    }

    @Transactional
    public void softDelete(Long userId) {
        if (!carrierDao.setDeleted(userId, RecordStatus.DELETED)) {
            log.error("Failed to soft delete carrier: no carrier found for userId={}", userId);
            throw new BadRequestException("No active carrier found for user with ID " + userId);
        }
        log.info("Successfully soft deleted carrier for userId={}", userId);
    }

    @Transactional
    public void softRestore(Long userId) {
        if (!carrierDao.setDeleted(userId, RecordStatus.ACTIVE)) {
            log.error("Failed to restore carrier: no deleted carrier found for userId={}", userId);
            throw new BadRequestException("No deleted carrier found for user with ID " + userId);
        }
        log.info("Successfully restored carrier for userId={}", userId);
    }

    @Transactional
    public void update(CarrierCreateDto carrierDto, Long userId) {
        if (!userService.hasCarrier(userId, RecordStatus.ACTIVE)) {
            log.error("Failed to update carrier: no active carrier found for userId={}", userId);
            throw new BadRequestException("No active carrier found for user with ID " + userId);
        }
        if (!carrierDao.update(userId, mapper.toEntity(carrierDto))) {
            log.error("Failed to update carrier: update failed for userId={}", userId);
            throw new BadRequestException("Failed to update carrier for user with ID " + userId);
        }
        log.info("Successfully updated carrier for userId={}", userId);
    }
}
