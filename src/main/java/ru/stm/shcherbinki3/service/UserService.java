package ru.stm.shcherbinki3.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.stm.shcherbinki3.dao.UserDao;
import ru.stm.shcherbinki3.dto.UserDto;
import ru.stm.shcherbinki3.model.type.RecordStatus;
import ru.stm.shcherbinki3.util.mapper.UserMapper;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDao userDao;
    private final UserMapper userMapper;

    public void create(UserDto dto) {
        userDao.create(userMapper.toEntity(dto));
    }

    public UserDto getById(Long id) {
        return userMapper.toDto(userDao.findByIdAndRecordStatus(id, RecordStatus.ACTIVE));
    }

    public UserDto update(UserDto dto) {
        return userMapper.toDto(userDao.update(userMapper.toEntity(dto)));
    }

    public void deleteById(Long id) {
        userDao.deleteById(id);
    }
}
