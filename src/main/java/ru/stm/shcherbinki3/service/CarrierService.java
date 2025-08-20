package ru.stm.shcherbinki3.service;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class CarrierService {

    private final CarrierDao carrierDao;
    private final UserService userService;
    private final CarrierMapper mapper;

    @Transactional
    public String create(CarrierCreateDto carrierDto, Long userId) {
        Optional<Carrier> existing = carrierDao.findByName(carrierDto.getName());

        // если такой name занят
        if (existing.isPresent()) {
            Carrier carrier = existing.get();

            if (carrier.getOwner()
                    .getId()
                    .equals(userId)) {
                // Если carrier в удаленных у этого же пользователя, то удаляем его окончательно и создаем новую
                if (carrier.getRecordStatus() == RecordStatus.DELETED) {
                    if (!carrierDao.hardDelete(userId)) {
                        throw new BadRequestException("Unknown error. Please repeat it again.");
                    }
                } else {
                    throw new SingletonCarrierForUser(userId);
                }
            } else {
                // Если истекло время сохранения -> то удаляем
                if (carrier.expiredDatetimeForSaveCarrier()) {
                    if (!carrierDao.hardDelete(userId)) {
                        throw new BadRequestException("Unknown error. Please repeat it again.");
                    }
                }
                // Иначе пока сохраняем за пользователем carrier name
                else {
                    throw new DuplicateCarrierName(carrierDto.getName());
                }
            }
        }

        if (userService.hasCarrier(userId, RecordStatus.ACTIVE)) {
            throw new SingletonCarrierForUser(userId);
        }

        Carrier carrier = carrierDao.create(mapper.toEntity(carrierDto), userId);
        return carrier.getName();
    }

    public CarrierWithRoutesDto getByUserId(Long userId) {
        return mapper.toDtoWithListRoutes(carrierDao.findWithRoutesByUserIdAndRecordStatus(userId, RecordStatus.ACTIVE)
                                                  .orElseThrow(() -> new ResourceNotFoundException(
                                                          "Carrier for user with id=%s not found".formatted(userId))));
    }

    @Transactional
    public void softDelete(Long userId) {
        boolean deleted = carrierDao.setDeleted(userId, RecordStatus.DELETED);
        if (!deleted) {
            throw new BadRequestException("The user does not have an associated carrier");
        }
    }

    @Transactional
    public void softRestore(Long userId) {
        boolean deleted = carrierDao.setDeleted(userId, RecordStatus.ACTIVE);
        if (!deleted) {
            throw new BadRequestException("The user does not have an associated carrier");
        }
    }

    public void update(CarrierCreateDto carrierDto, Long userId) {
        if (!userService.hasCarrier(userId, RecordStatus.ACTIVE)) {
            throw new BadRequestException("The user does not have a carrier");
        }
        if (!carrierDao.update(userId, mapper.toEntity(carrierDto))) {
            throw new BadRequestException("The carrier has not been updated");
        }

    }
}
