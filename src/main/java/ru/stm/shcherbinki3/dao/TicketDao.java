package ru.stm.shcherbinki3.dao;

import jakarta.validation.constraints.NotNull;
import ru.stm.shcherbinki3.model.Ticket;
import ru.stm.shcherbinki3.util.pagination.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface TicketDao {

    String TABLE_NAME = "ticket";

    Set<String> ALLOWED_SORT_COLUMNS = Set.of("id", "place_number", "departure_datetime", "price");

    List<Ticket> findAllByRouteId(@NotNull Long routeId, LocalDate date, @NotNull Pageable pageable);

    List<Ticket> findAllByUserId(@NotNull Long userId, LocalDate after, LocalDate before, @NotNull Pageable pageable);

    void createAll(@NotNull Long routeId, List<Ticket> ticketList);

    boolean assignTicketToUser(Long userId, Long ticketId);

    long countByParameters(@NotNull Long routeId, LocalDate date);

    long countByParameters(@NotNull Long userId, LocalDate after, LocalDate before, @NotNull Pageable pageable);

}
