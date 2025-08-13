package ru.stm.shcherbinki3.util.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import ru.stm.shcherbinki3.dto.UserDto;
import ru.stm.shcherbinki3.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "tickets", ignore = true)
    @Mapping(target = "carrier", ignore = true)
    User toEntity(UserDto dto);

    @Mapping(target = "id", source = "id")
    UserDto toDto(User entity);

    List<User> toEntityList(List<UserDto> dtoList);

    List<UserDto> toDtoList(List<User> entityList);

    @Mapping(target = "tickets", ignore = true)
    @Mapping(target = "carrier", ignore = true)
    void updateEntityFromDto(UserDto dto, @MappingTarget User entity);
}
