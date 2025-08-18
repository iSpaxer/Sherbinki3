package ru.stm.shcherbinki3.dao;

import ru.stm.shcherbinki3.model.Ticket;

import java.util.List;

public interface TicketDao {

    String TABLE_NAME = "ticket";

    List<Ticket> findAllByUserId(Long userId);

    void createAll(Long routeId, List<Ticket> ticketList);

}
