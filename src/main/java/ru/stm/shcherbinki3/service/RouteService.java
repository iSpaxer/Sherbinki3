package ru.stm.shcherbinki3.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.stm.shcherbinki3.dao.RouteDao;
import ru.stm.shcherbinki3.dao.UserDao;
import ru.stm.shcherbinki3.dto.route.RouteWithCarrierDto;
import ru.stm.shcherbinki3.model.Route;
import ru.stm.shcherbinki3.util.exception.BadRequestException;
import ru.stm.shcherbinki3.util.mapper.RouteMapper;
import ru.stm.shcherbinki3.util.pagination.PageResponse;
import ru.stm.shcherbinki3.util.pagination.Pageable;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteDao routeDao;
    private final UserDao userDao;
    private final RouteMapper routeMapper;

    public Long create(Long userId, RouteWithCarrierDto dto) {
        Route route = routeDao.create(userId, routeMapper.toEntity(dto));
        return route.getId();
    }

    public PageResponse<RouteWithCarrierDto> getRoutes(String carrierName, String departure, String destination,
                                                       LocalDate date, Pageable pageable) {
        List<Route> routeList = routeDao.findByParameters(carrierName, departure, destination, date, pageable);
        long total = routeDao.countByParameters(carrierName, departure, destination, date);
        return new PageResponse<>(routeMapper.toDtoList(routeList), pageable.page(), pageable.size(), total);
    }

    @Transactional
    public void deleteByRouteId(Long userId, Long routeId) {
        if (!userDao.isOwnerOfRoute(userId, routeId)) {
            throw new BadRequestException("Mistake. You can't delete a route.");
        }
        routeDao.delete(routeId);
    }

    public void updateRoute(Long userId, Long routeId, Long durationMinutes) {
        if (!userDao.isOwnerOfRoute(userId, routeId)) {
            throw new BadRequestException("Mistake. You can't delete a route.");
        }
        routeDao.updateDurationMinutes(routeId, durationMinutes);
    }
}
