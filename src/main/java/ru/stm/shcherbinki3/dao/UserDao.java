package ru.stm.shcherbinki3.dao;

import ru.stm.shcherbinki3.model.User;
import ru.stm.shcherbinki3.model.type.RecordStatus;

import java.util.Optional;

public interface UserDao {

    Optional<User> findById(Long id);
    Optional<User> findByIdAndRecordStatus(Long id, RecordStatus recordStatus);
    Optional<User> findByEmailAndRecordStatus(String email, RecordStatus recordStatus);
    User create(User user);
    boolean update(User user);
    boolean deleteById(Long id);

}
