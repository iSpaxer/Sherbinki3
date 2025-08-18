package ru.stm.shcherbinki3.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.stm.shcherbinki3.dao.TicketDao;
import ru.stm.shcherbinki3.dto.ticket.TicketCreateDto;
import ru.stm.shcherbinki3.model.Ticket;
import ru.stm.shcherbinki3.util.mapper.TicketMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketDao ticketDao;
    private final TicketMapper ticketMapper;

    public void create(Long userId, Long routeId, TicketCreateDto dto) {
        List<Ticket> ticketList = ticketMapper.toEntityList(dto);
        ticketDao.createAll(userId, routeId, ticketList);
    }
}
