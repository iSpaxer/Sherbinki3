package ru.stm.shcherbinki3.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.stm.shcherbinki3.dao.RouteDao;
import ru.stm.shcherbinki3.dto.route.RouteDto;
import ru.stm.shcherbinki3.model.Route;
import ru.stm.shcherbinki3.util.mapper.RouteMapper;
import ru.stm.shcherbinki3.util.pagination.PageResponse;
import ru.stm.shcherbinki3.util.pagination.Pageable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteDao routeDao;
    private final RouteMapper routeMapper;

    public Long create(Long userId, RouteDto dto) {
        Route route = routeDao.create(userId, routeMapper.toEntity(dto));
        return route.getId();
    }

    public PageResponse<RouteDto> getRoutes(String carrierName, String departure, String destination, Pageable pageable) {
        List<Route> routeList = routeDao.findByParameters(carrierName, departure, destination, pageable);
        long total = routeDao.countByParameters(carrierName, departure, destination);
        return new PageResponse<>(routeMapper.toDtoList(routeList), pageable.page(), pageable.size(), total);
    }
}
