package ru.stm.shcherbinki3.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.stm.shcherbinki3.dao.UserDao;
import ru.stm.shcherbinki3.model.User;
import ru.stm.shcherbinki3.model.type.RecordStatus;
import ru.stm.shcherbinki3.util.exception.BadRequestException;

import java.util.*;

@Repository
public class UserDaoImpl implements UserDao {

    public final String TABLE_NAME = "app_user";
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    @Autowired
    public UserDaoImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(TABLE_NAME)
                .usingGeneratedKeyColumns("id")
                .usingColumns("email", "password", "name", "lastname", "patronymic");
    }

    @Override
    public Optional<User> findById(Long id) {
        return findByIdAndRecordStatus(id, RecordStatus.ACTIVE);
    }

    @Override
    public Optional<User> findByIdAndRecordStatus(Long id, RecordStatus recordStatus) {
        String sql = """
                SELECT id, email, password, name, lastname, patronymic FROM %s
                WHERE id = :id AND record_status = :status
                """.formatted(TABLE_NAME);
        Map<String, Object> params = Map.of("id", id, "status", recordStatus.name());

        return doRequestInBd(sql, params);
    }

    private Optional<User> doRequestInBd(String sql, Map<String, Object> params) {
        try {
            return Optional.of(namedParameterJdbcTemplate.queryForObject(
                    sql,
                    params,
                    new BeanPropertyRowMapper<>(User.class)
            ));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByEmailAndRecordStatus(String email, RecordStatus recordStatus) {
        String sql = """
                SELECT id, email, password, name, lastname, patronymic FROM %s
                WHERE email = :email AND record_status = :status
                """.formatted(TABLE_NAME);
        Map<String, Object> params = Map.of("email", email, "status", recordStatus.name());
        return doRequestInBd(sql, params);
    }

    @Override
    public User create(User user) {
        SqlParameterSource parameters = new BeanPropertySqlParameterSource(user);
        Number id = simpleJdbcInsert.executeAndReturnKey(parameters);
        user.setId(id.longValue());
        return user;
    }

    @Override
    public boolean update(User user) {
        Map<String, Object> params = new HashMap<>();
        StringBuilder sql = new StringBuilder("UPDATE %s SET".formatted(TABLE_NAME));

        List<String> setClauses = new ArrayList<>();

        if (user.getPassword() != null) {
            setClauses.add("password = :password");
            params.put("password", user.getPassword());
        }
        if (user.getName() != null) {
            setClauses.add("name = :name");
            params.put("name", user.getName());
        }
        if (user.getLastname() != null) {
            setClauses.add("lastname = :lastname");
            params.put("lastname", user.getLastname());
        }
        if (user.getPatronymic() != null) {
            setClauses.add("patronymic = :patronymic");
            params.put("patronymic", user.getPatronymic());
        }

        if (setClauses.isEmpty()) {
            throw new IllegalArgumentException("At least one field must be provided for update");
        }

        sql.append(String.join(", ", setClauses));

        // Нельзя обновлять удаленные аккаунты
        sql.append(" WHERE id = :id AND record_status = 'ACTIVE'");
        params.put("id", user.getId());

        return namedParameterJdbcTemplate.update(sql.toString(), new MapSqlParameterSource(params)) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "UPDATE %s SET record_status = 'DELETED' WHERE id = :id AND record_status = 'ACTIVE'"
                .formatted(TABLE_NAME);
        Map<String, Object> params = Map.of("id", id);
        return namedParameterJdbcTemplate.update(sql, params) > 0;
//        int rowsAffected = namedParameterJdbcTemplate.update(sql, new MapSqlParameterSource(params));
//        if (rowsAffected == 0) {
//            throw new BadRequestException("User does not exist.");
//        }
    }

}
