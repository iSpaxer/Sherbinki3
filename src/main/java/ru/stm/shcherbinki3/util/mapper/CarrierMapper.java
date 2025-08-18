package ru.stm.shcherbinki3.util.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.stm.shcherbinki3.dto.carrier.CarrierDto;
import ru.stm.shcherbinki3.dto.carrier.CarrierWithRoutesDto;
import ru.stm.shcherbinki3.model.Carrier;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CarrierMapper {

    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "deletedDatetime", ignore = true)
    @Mapping(target = "routeList", ignore = true)
    Carrier toEntity(CarrierDto dto);

    CarrierDto toDto(Carrier entity);

    CarrierWithRoutesDto toDtoWithListRoutes(Carrier entity);

    List<Carrier> toEntityList(List<CarrierDto> dtoList);

    List<CarrierDto> toDtoList(List<Carrier> entityList);

    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "deletedDatetime", ignore = true)
    @Mapping(target = "routeList", ignore = true)
    void updateEntityFromDto(CarrierDto dto, @MappingTarget Carrier entity);
}
