package ru.stm.shcherbinki3.dao.impl;

import org.springframework.stereotype.Repository;
import ru.stm.shcherbinki3.dao.TicketDao;
import ru.stm.shcherbinki3.model.Ticket;

import java.util.List;

@Repository
public class TicketDaoImpl implements TicketDao {
    @Override
    public List<Ticket> findAllByUserId(Long userId) {
        return null;
    }

    @Override
    public void createAll(Long userId, Long routeId, List<Ticket> ticketList) {

    }
}
