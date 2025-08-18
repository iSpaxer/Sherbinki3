package ru.stm.shcherbinki3.util.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.stm.shcherbinki3.dto.route.RouteDto;
import ru.stm.shcherbinki3.model.Route;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RouteMapper {

    Route toEntity(RouteDto dto);

    RouteDto toDto(Route entity);

    List<Route> toEntityList(List<RouteDto> dtoList);

    List<RouteDto> toDtoList(List<Route> entityList);

    void updateEntityFromDto(RouteDto dto, @MappingTarget Route entity);

}
