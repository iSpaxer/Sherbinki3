package ru.stm.shcherbinki3.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.stm.shcherbinki3.dao.RouteDao;
import ru.stm.shcherbinki3.dao.UserDao;
import ru.stm.shcherbinki3.dto.route.RouteWithCarrierDto;
import ru.stm.shcherbinki3.model.Route;
import ru.stm.shcherbinki3.util.exception.ForbiddenException;
import ru.stm.shcherbinki3.util.exception.ResourceNotFoundException;
import ru.stm.shcherbinki3.util.mapper.RouteMapper;
import ru.stm.shcherbinki3.util.pagination.PageResponse;
import ru.stm.shcherbinki3.util.pagination.Pageable;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RouteService {

    private final RouteDao routeDao;
    private final UserDao userDao;
    private final RouteMapper routeMapper;

    @Transactional
    public Long create(Long userId, RouteWithCarrierDto dto) {
        Route route = routeDao.create(userId, routeMapper.toEntity(dto));
        log.info("Successfully created route with id={} for userId={}", route.getId(), userId);
        return route.getId();
    }

    @Transactional(readOnly = true)
    public PageResponse<RouteWithCarrierDto> getRoutes(String carrierName, String departure, String destination,
                                                       LocalDate date, Pageable pageable) {
        List<Route> routeList = routeDao.findByParameters(carrierName, departure, destination, date, pageable);
        long total = routeDao.countByParameters(carrierName, departure, destination, date);
        log.info("Successfully retrieved {} routes with filters: carrierName={}, departure={}, destination={}, date={}",
                 routeList.size(), carrierName, departure, destination, date);
        return new PageResponse<>(routeMapper.toDtoList(routeList), pageable.page(), pageable.size(), total);
    }

    @Transactional
    public void deleteByRouteId(Long userId, Long routeId) {
        if (!routeDao.existsById(routeId)) {
            log.error("Failed to delete route: routeId={} not found", routeId);
            throw new ResourceNotFoundException("Route with ID " + routeId + " not found");
        }
        if (!userDao.isOwnerOfRoute(userId, routeId)) {
            log.error("Failed to delete route: userId={} is not the owner of routeId={}", userId, routeId);
            throw new ForbiddenException("User with ID " + userId + " does not have access to route with ID " + routeId);
        }
        routeDao.delete(routeId);
        log.info("Successfully deleted routeId={} for userId={}", routeId, userId);
    }

    @Transactional
    public void updateRoute(Long userId, Long routeId, Long durationMinutes) {
        if (!routeDao.existsById(routeId)) {
            log.error("Failed to update route: routeId={} not found", routeId);
            throw new ResourceNotFoundException("Route with ID " + routeId + " not found");
        }
        if (!userDao.isOwnerOfRoute(userId, routeId)) {
            log.error("Failed to update route: userId={} is not the owner of routeId={}", userId, routeId);
            throw new ForbiddenException("User with ID " + userId + " does not have access to route with ID " + routeId);
        }
        routeDao.updateDurationMinutes(routeId, durationMinutes);
        log.info("Successfully updated routeId={} duration to {} minutes for userId={}", routeId, durationMinutes, userId);
    }
}
