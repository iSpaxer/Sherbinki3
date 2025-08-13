package ru.stm.shcherbinki3.dao;

import ru.stm.shcherbinki3.model.Route;

import java.util.List;
import java.util.Optional;

// todo Criteria API
public interface RouteDao {
    List<Route> findAllByDepartureAndDestination(String departure, String destination);

}
