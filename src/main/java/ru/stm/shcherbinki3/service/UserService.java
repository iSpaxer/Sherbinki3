package ru.stm.shcherbinki3.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.stm.shcherbinki3.dao.UserDao;
import ru.stm.shcherbinki3.dto.UserDto;
import ru.stm.shcherbinki3.model.User;
import ru.stm.shcherbinki3.model.type.RecordStatus;
import ru.stm.shcherbinki3.security.JwtUserDetails;
import ru.stm.shcherbinki3.util.exception.ResourceNotFoundException;
import ru.stm.shcherbinki3.util.exception.business.DuplicateEmailException;
import ru.stm.shcherbinki3.util.exception.business.EmailUsedByDeletedUserException;
import ru.stm.shcherbinki3.util.mapper.UserMapper;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserDao userDao;
    private final UserMapper userMapper;

    public static String generateJti() {
        return UUID.randomUUID().toString();
    }

    @Transactional
    public Long create(UserDto dto) {
        try {
            User user = userMapper.toEntity(dto);
            userDao.create(user);
            log.debug("User created with id={}", user.getId());
            return user.getId();
        } catch (DuplicateKeyException ex) {
            if (ex.getMessage().contains("app_user_email_key")) {
                if (userDao.findByEmailAndRecordStatus(dto.getEmail(), RecordStatus.DELETED).isPresent()) {
                    log.warn("Email {} is used by a deleted user", dto.getEmail());
                    throw new EmailUsedByDeletedUserException(dto.getEmail());
                }
                log.warn("Email {} is already taken", dto.getEmail());
                throw new DuplicateEmailException("Email is already taken");
            }
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public UserDto getById(Long id) {
        User user = userDao.findByIdAndRecordStatus(id, RecordStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.warn("User not found with id={}", id);
                    return new ResourceNotFoundException("User not found with id: " + id);
                });
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto update(Long id, UserDto dto) {
        User user = userMapper.toEntity(dto);
        boolean updated = userDao.update(id, user);
        if (!updated) {
            log.warn("User not found with id={}", id);
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        log.debug("User updated with id={}", id);
        return userMapper.toDto(user);
    }

    @Transactional
    public void setDeleted(Long id, RecordStatus status) {
        boolean deleted = userDao.setDeleted(id, status);
        if (!deleted) {
            log.warn("User not found or already deleted with id={}", id);
            throw new ResourceNotFoundException("User not found or already deleted with id: " + id);
        }
        log.debug("User status updated to {} for id={}", status, id);
    }

    @Transactional(readOnly = true)
    public boolean hasCarrier(Long id, RecordStatus userRecordStatus) {
        return userDao.hasCarrier(id, userRecordStatus);
    }

    public UserDetails getUserDetailsByEmail(String email) {
        User user = userDao.findByEmailAndRecordStatus(email, RecordStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User with email = %s not found".formatted(email)));

        return new JwtUserDetails(user.getId(), user.getEmail(), user.getPassword(), generateJti());
    }
}
