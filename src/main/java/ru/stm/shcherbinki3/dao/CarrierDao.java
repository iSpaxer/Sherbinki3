package ru.stm.shcherbinki3.dao;

import ru.stm.shcherbinki3.model.Carrier;
import ru.stm.shcherbinki3.model.type.RecordStatus;

import java.util.Optional;

public interface CarrierDao {
    String TABLE_NAME = "carrier";

    Optional<Carrier> findByUserIdAndRecordStatus(Long userId, RecordStatus recordStatus);

    Optional<Carrier> findWithRoutesByUserIdAndRecordStatus(Long userId, RecordStatus recordStatus);

    Optional<Carrier> findByName(String name);

    Carrier create(Carrier carrier, Long userId);

    boolean hardDelete(Long userId);

    boolean setDeleted(Long userId, RecordStatus recordStatus);

}
