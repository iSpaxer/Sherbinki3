package ru.stm.shcherbinki3.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ru.stm.shcherbinki3.util.exception.ResourceNotFoundException;
import ru.stm.shcherbinki3.util.mapper.TicketMapper;
import ru.stm.shcherbinki3.util.pagination.PageResponse;
import ru.stm.shcherbinki3.util.pagination.Pageable;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TicketService {

    private final TicketDao ticketDao;
    private final UserDao userDao;
    private final TicketMapper ticketMapper;

    @Transactional
    public void create(Long userId, Long routeId, TicketCreateDto dto) {
        if (!userDao.isOwnerOfRoute(userId, routeId)) {
            log.error("Failed to create tickets: userId={} is not the owner of routeId={}", userId, routeId);
            throw new ForbiddenException("User with ID " + userId + " does not have access to route with ID " + routeId);
        }
        List<Ticket> ticketList = ticketMapper.toEntityList(dto);
        ticketDao.createAll(routeId, ticketList);
        log.info("Successfully created tickets for routeId={}", routeId);
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketPublicDto> getTickets(Long routeId, LocalDate date, Pageable pageable) {
        List<Ticket> ticketList = ticketDao.findAllByRouteId(routeId, date, pageable);
        if (ticketList.isEmpty() && !ticketDao.existsByRouteId(routeId)) {
            log.error("Failed to get tickets: routeId={} not found", routeId);
            throw new ResourceNotFoundException("Route with ID " + routeId + " not found");
        }
        long total = ticketDao.countByParameters(routeId, date);
        log.info("Successfully retrieved {} tickets for routeId={}", ticketList.size(), routeId);
        return new PageResponse<>(ticketMapper.toDtoList(ticketList), pageable.page(), pageable.size(), total);
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketPurchasedDto> getTicketsByUser(Long userId, LocalDate after, LocalDate before, Pageable pageable) {
        List<Ticket> ticketList = ticketDao.findAllByUserId(userId, after, before, pageable);
        long total = ticketDao.countByParameters(userId, after, before, pageable);
        log.info("Successfully retrieved {} purchased tickets for userId={}", ticketList.size(), userId);
        return new PageResponse<>(ticketMapper.toDtoPurchasedList(ticketList), pageable.page(), pageable.size(), total);
    }

    @Transactional
    public void buyTicket(Long userId, Long ticketId) {
        if (!ticketDao.existsById(ticketId)) {
            log.error("Failed to buy ticket: ticketId={} not found", ticketId);
            throw new ResourceNotFoundException("Ticket with ID " + ticketId + " not found");
        }
        userDao.findByTicketId(ticketId).ifPresentOrElse(user -> {
            log.error("Failed to buy ticket: ticketId={} is already purchased", ticketId);
            throw new BadRequestException("Ticket with ID " + ticketId + " has already been purchased");
        }, () -> {
            if (!ticketDao.assignTicketToUser(userId, ticketId)) {
                log.error("Failed to buy ticket: could not assign ticketId={} to userId={}", ticketId, userId);
                throw new BadRequestException("Failed to purchase ticket with ID " + ticketId);
            }
            log.info("Successfully purchased ticketId={} for userId={}", ticketId, userId);
        });
    }

    @Transactional
    public void returnTicket(Long userId, Long ticketId) {
        if (!ticketDao.existsById(ticketId)) {
            log.error("Failed to return ticket: ticketId={} not found", ticketId);
            throw new ResourceNotFoundException("Ticket with ID " + ticketId + " not found");
        }
        userDao.findByTicketId(ticketId).ifPresentOrElse(user -> {
            if (!user.getId().equals(userId)) {
                log.error("Failed to return ticket: ticketId={} not owned by userId={}", ticketId, userId);
                throw new BadRequestException("User with ID " + userId + " cannot return ticket with ID " + ticketId + " as it was not purchased by them");
            }
            if (!ticketDao.assignTicketToUser(null, ticketId)) {
                log.error("Failed to return ticket: could not unassign ticketId={} for userId={}", ticketId, userId);
                throw new BadRequestException("Failed to return ticket with ID " + ticketId);
            }
            log.info("Successfully returned ticketId={} for userId={}", ticketId, userId);
        }, () -> {
            log.error("Failed to return ticket: ticketId={} has not been purchased", ticketId);
            throw new BadRequestException("Ticket with ID " + ticketId + " has not been purchased");
        });
    }
}
