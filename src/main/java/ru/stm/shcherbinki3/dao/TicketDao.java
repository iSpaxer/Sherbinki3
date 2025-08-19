package ru.stm.shcherbinki3.dao;

import ru.stm.shcherbinki3.model.Ticket;
import ru.stm.shcherbinki3.util.pagination.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface TicketDao {

    String TABLE_NAME = "ticket";

    Set<String> ALLOWED_SORT_COLUMNS = Set.of("id", "place_number", "departure_datetime", "price");

    List<Ticket> findAllByRouteId(Long routeId, LocalDate date, Pageable pageable);

    List<Ticket> findAllByUserId(Long userId, LocalDate after, LocalDate before, Pageable pageable);

    void createAll(Long routeId, List<Ticket> ticketList);

    long countByParameters(Long routeId, LocalDate date);

    long countByParameters(Long userId, LocalDate after, LocalDate before, Pageable pageable);

}
