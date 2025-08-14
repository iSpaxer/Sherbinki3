package ru.stm.shcherbinki3.dao;

import ru.stm.shcherbinki3.model.User;
import ru.stm.shcherbinki3.model.type.RecordStatus;

public interface UserDao {
    User findById(Long id);
    User findByIdAndRecordStatus(Long id, RecordStatus recordStatus);
    User create(User user);
    User update(User user);
    void deleteById(Long id);

}
