package ru.stm.shcherbinki3.dao;

import org.springframework.stereotype.Repository;
import ru.stm.shcherbinki3.model.User;

import java.util.Optional;

public interface UserDao {
    User findById(Long id);
    void create(User user);
    void update(User user);
    void deleteById(Long id);

}
