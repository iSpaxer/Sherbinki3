package ru.stm.shcherbinki3.dao;

import ru.stm.shcherbinki3.model.Route;
import ru.stm.shcherbinki3.util.pagination.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface RouteDao {

    String TABLE_NAME = "route";
    Set<String> ALLOWED_SORT_COLUMNS = Set.of("id", "departure", "destination", "duration_minutes");

    Route create(Long userId, Route entity);

    List<Route> findByParameters(String carrierName, String departure, String destination, LocalDate date, Pageable pageable);

    long countByParameters(String carrierName, String departure, String destination, LocalDate date);

}
