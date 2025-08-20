package ru.stm.shcherbinki3.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.stm.shcherbinki3.dao.TicketDao;
import ru.stm.shcherbinki3.dao.UserDao;
import ru.stm.shcherbinki3.dto.ticket.TicketCreateDto;
import ru.stm.shcherbinki3.dto.ticket.TicketPublicDto;
import ru.stm.shcherbinki3.dto.ticket.TicketPurchasedDto;
import ru.stm.shcherbinki3.model.Ticket;
import ru.stm.shcherbinki3.util.exception.BadRequestException;
import ru.stm.shcherbinki3.util.exception.ForbiddenException;
import ru.stm.shcherbinki3.util.mapper.TicketMapper;
import ru.stm.shcherbinki3.util.pagination.PageResponse;
import ru.stm.shcherbinki3.util.pagination.Pageable;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketDao ticketDao;
    private final UserDao userDao;
    private final TicketMapper ticketMapper;

    public void create(Long userId, Long routeId, TicketCreateDto dto) {
        List<Ticket> ticketList = ticketMapper.toEntityList(dto);
        if (userDao.isOwnerOfRoute(userId, routeId)) {
            ticketDao.createAll(routeId, ticketList);
        } else {
            throw new ForbiddenException(
                    "User with ID " + userId + " does not have access to route with ID " + routeId);
        }
    }

    public PageResponse<TicketPublicDto> getTickets(Long routeId,
                                                    LocalDate date,
                                                    Pageable pageable) {
        List<Ticket> ticketList = ticketDao.findAllByRouteId(routeId, date, pageable);
        long total = ticketDao.countByParameters(routeId, date);

        return new PageResponse<>(ticketMapper.toDtoList(ticketList), pageable.page(), pageable.size(), total);
    }

    public PageResponse<TicketPurchasedDto> getTicketsByUser(Long userId, LocalDate after, LocalDate before,
                                                             Pageable pageable) {
        List<Ticket> ticketList = ticketDao.findAllByUserId(userId, after, before, pageable);
        long total = ticketDao.countByParameters(userId, after, before, pageable);

        return new PageResponse<>(ticketMapper.toDtoPurchasedList(ticketList), pageable.page(), pageable.size(), total);
    }

    @Transactional
    public void buyTicket(Long userId, Long ticketId) {
        userDao.findByTicketId(ticketId)
                .ifPresentOrElse(user -> {
                    throw new BadRequestException("The ticket has already been purchased");
                }, () -> {
                    if (!ticketDao.assignTicketToUser(userId, ticketId)) {
                        throw new BadRequestException("Couldn't buy a ticket.");
                    }
                });
    }

    @Transactional
    public void returnTicket(Long userId, Long ticketId) {
        userDao.findByTicketId(ticketId)
                .ifPresentOrElse(user -> {
                    if (user.getId().equals(userId)) {
                        if (!ticketDao.assignTicketToUser(null, ticketId)) {
                            throw new BadRequestException("The ticket could not be refunded.");
                        }
                        return;
                    }
                    throw new BadRequestException("You cannot refund the money for the ticket. The ticket was not purchased by you");
                }, () -> {
                    throw new BadRequestException("The ticket has not been purchased yet");
                });
    }
}
