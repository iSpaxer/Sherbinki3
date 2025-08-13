package ru.stm.shcherbinki3.dao;

import ru.stm.shcherbinki3.model.User;
import ru.stm.shcherbinki3.util.sql.UserSqlRequests;

public class UserDaoImpl implements UserDao {

    private final UserSqlRequests sql = new UserSqlRequests();

    @Override
    public User findById(Long id) {
        return null;
    }

    @Override
    public void create(User user) {

    }

    @Override
    public void update(User user) {

    }

    @Override
    public void deleteById(Long id) {

    }
}
