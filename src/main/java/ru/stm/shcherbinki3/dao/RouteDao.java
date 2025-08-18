package ru.stm.shcherbinki3.dao;

import ru.stm.shcherbinki3.model.Route;
import ru.stm.shcherbinki3.util.pagination.Pageable;

import java.util.List;

// todo Criteria API
public interface RouteDao {

    String TABLE_NAME = "route";

    Route create(Long userId, Route entity);

    List<Route> findByParameters(String carrierName, String departure, String destination, Pageable pageable);

    long countByParameters(String carrierName, String departure, String destination);
}
