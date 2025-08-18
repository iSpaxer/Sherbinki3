package ru.stm.shcherbinki3.util.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.stm.shcherbinki3.dto.route.RouteWithCarrierDto;
import ru.stm.shcherbinki3.model.Route;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RouteMapper {

    Route toEntity(RouteWithCarrierDto dto);

    RouteWithCarrierDto toDto(Route entity);

    List<Route> toEntityList(List<RouteWithCarrierDto> dtoList);

    List<RouteWithCarrierDto> toDtoList(List<Route> entityList);

    void updateEntityFromDto(RouteWithCarrierDto dto, @MappingTarget Route entity);

}
