package ru.stm.shcherbinki3.util.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.stm.shcherbinki3.dto.UserDto;
import ru.stm.shcherbinki3.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "tickets", ignore = true)
    @Mapping(target = "carrier", ignore = true)
    @Mapping(target = "recordStatus", ignore = true)
    User toEntity(UserDto dto);

    @Mapping(target = "id", source = "id") // todo наверное нужно удалить
    UserDto toDto(User entity);

    List<User> toEntityList(List<UserDto> dtoList);

    List<UserDto> toDtoList(List<User> entityList);

    @Mapping(target = "tickets", ignore = true)
    @Mapping(target = "carrier", ignore = true)
    @Mapping(target = "recordStatus", ignore = true)
    void updateEntityFromDto(UserDto dto, @MappingTarget User entity);
}
