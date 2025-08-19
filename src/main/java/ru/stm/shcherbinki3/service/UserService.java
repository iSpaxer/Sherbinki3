package ru.stm.shcherbinki3.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.stm.shcherbinki3.dao.UserDao;
import ru.stm.shcherbinki3.dto.UserDto;
import ru.stm.shcherbinki3.model.User;
import ru.stm.shcherbinki3.model.type.RecordStatus;
import ru.stm.shcherbinki3.util.exception.BadRequestException;
import ru.stm.shcherbinki3.util.exception.ResourceNotFoundException;
import ru.stm.shcherbinki3.util.exception.business.DuplicateEmailException;
import ru.stm.shcherbinki3.util.exception.business.EmailUsedByDeletedUserException;
import ru.stm.shcherbinki3.util.mapper.UserMapper;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDao userDao;
    private final UserMapper userMapper;

    @Transactional
    public Long create(UserDto dto) {
        try {
            User user = userDao.create(userMapper.toEntity(dto));
            return user.getId();
        } catch (DuplicateKeyException ex) {
            if (ex.getMessage()
                    .contains("app_user_email_key")) {
                userDao.findByEmailAndRecordStatus(dto.getEmail(), RecordStatus.DELETED)
                        .ifPresentOrElse(
                                user -> {
                                    throw new EmailUsedByDeletedUserException(dto.getEmail());
                                },
                                () -> {
                                    throw new DuplicateEmailException("Email is already taken");
                                }

                        );
            }
            throw ex;
        }
    }

    public UserDto getById(Long id) {
        return userMapper.toDto(
                userDao.findByIdAndRecordStatus(id, RecordStatus.ACTIVE)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id))
        );
    }

    @Transactional
    public UserDto update(Long id, UserDto dto) {
        boolean updated = userDao.update(id, userMapper.toEntity(dto));

        if (!updated) {
            throw new ResourceNotFoundException("User not found with id: " + dto.getId());
        }

        return getById(id);
    }

    @Transactional
    public void setDeleted(Long id, RecordStatus status) {
        boolean deleted = userDao.setDeleted(id, status);
        if (!deleted) {
            throw new BadRequestException("User does not exist or already deleted");
        }
    }

    public boolean hasCarrier(Long id, RecordStatus userRecordStatus) {
        return userDao.hasCarrier(id, userRecordStatus);
    }

}
