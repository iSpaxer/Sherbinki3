package ru.stm.shcherbinki3.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.stm.shcherbinki3.dao.CarrierDao;
import ru.stm.shcherbinki3.dao.RouteDao;
import ru.stm.shcherbinki3.dao.TicketDao;
import ru.stm.shcherbinki3.dao.UserDao;
import ru.stm.shcherbinki3.model.User;
import ru.stm.shcherbinki3.model.type.RecordStatus;
import ru.stm.shcherbinki3.util.sql.SqlQueryBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class UserDaoImpl implements UserDao {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    @Autowired
    public UserDaoImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(namedParameterJdbcTemplate.getJdbcTemplate())
                .withTableName(TABLE_NAME)
                .usingGeneratedKeyColumns("id")
                .usingColumns("email", "password", "name", "lastname", "patronymic");
    }

    @Override
    public Optional<User> findByIdAndRecordStatus(Long id, RecordStatus recordStatus) {
        SqlQueryBuilder builder = new SqlQueryBuilder("""
                SELECT id, email, password, name, lastname, patronymic, record_status
                FROM %s
                WHERE 1=1
                """.formatted(TABLE_NAME))
                .addFilter("id = :id", "id", id)
                .addFilter("record_status = :status", "status", recordStatus.name());

        try {
            User user = namedParameterJdbcTemplate.queryForObject(
                    builder.getSql(),
                    builder.getParams(),
                    new BeanPropertyRowMapper<>(User.class)
            );
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByEmailAndRecordStatus(String email, RecordStatus recordStatus) {
        SqlQueryBuilder builder = new SqlQueryBuilder("""
                SELECT id, email, password, name, lastname, patronymic, record_status
                FROM %s
                WHERE 1=1
                """.formatted(TABLE_NAME))
                .addFilter("email = :email", "email", email)
                .addFilter("record_status = :status", "status", recordStatus.name());

        try {
            User user = namedParameterJdbcTemplate.queryForObject(
                    builder.getSql(),
                    builder.getParams(),
                    new BeanPropertyRowMapper<>(User.class)
            );
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByTicketId(Long ticketId) {
        SqlQueryBuilder builder = new SqlQueryBuilder("""
            SELECT u.id, u.email, u.name, u.lastname, u.patronymic
            FROM %s u
            LEFT JOIN %s t ON t.user_id = u.id
            WHERE 1=1
            """.formatted(TABLE_NAME, TicketDao.TABLE_NAME))
                .addFilter("t.id = :ticketId", "ticketId", ticketId);

        try {
            User user = namedParameterJdbcTemplate.queryForObject(
                    builder.getSql(),
                    builder.getParams(),
                    new BeanPropertyRowMapper<>(User.class)
            );
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public User create(User user) {
        SqlParameterSource parameters = new BeanPropertySqlParameterSource(user);
        Number id = simpleJdbcInsert.executeAndReturnKey(parameters);
        user.setId(id.longValue());
        return user;
    }

    @Override
    public boolean update(Long id, User user) {
        Map<String, Object> fields = new HashMap<>();
        if (user.getPassword() != null) fields.put("password", user.getPassword());
        if (user.getName() != null) fields.put("name", user.getName());
        if (user.getLastname() != null) fields.put("lastname", user.getLastname());
        if (user.getPatronymic() != null) fields.put("patronymic", user.getPatronymic());

        SqlQueryBuilder builder = new SqlQueryBuilder("UPDATE %s".formatted(TABLE_NAME))
                .addUpdateFields(fields)
                .addFilterWhere("id = :id", "id", id)
                .addFilter("record_status = 'ACTIVE'");

        return namedParameterJdbcTemplate.update(builder.getSql(), builder.getParams()) > 0;
    }

    @Override
    public boolean setDeleted(Long id, RecordStatus status) {
        SqlQueryBuilder builder = new SqlQueryBuilder("UPDATE %s".formatted(TABLE_NAME))
                .addUpdateFields(Map.of("record_status", status.name()))
                .addFilterWhere(" id = :id", "id", id)
                .addFilter("record_status != :status", "status", status.name());

        return namedParameterJdbcTemplate.update(builder.getSql(), builder.getParams()) > 0;
    }

    @Override
    public boolean hasCarrier(Long id, RecordStatus userRecordStatus) {
        SqlQueryBuilder builder = new SqlQueryBuilder("""
                SELECT
                    CASE WHEN u.carrier_id IS NOT NULL AND c.record_status = :status
                    THEN true
                    ELSE false
                END
                FROM %s u
                LEFT JOIN %s c ON c.id = u.carrier_id
                WHERE 1=1
                """.formatted(TABLE_NAME, CarrierDao.TABLE_NAME))
                .addValue("status", userRecordStatus.name())
                .addFilter("u.id = :userId", "userId", id);

        Boolean result = namedParameterJdbcTemplate.queryForObject(
                builder.getSql(),
                builder.getParams(),
                Boolean.class
        );
        return Boolean.TRUE.equals(result);
    }

    @Override
    public boolean isOwnerOfRoute(Long userId, Long routeId) {
        SqlQueryBuilder builder = new SqlQueryBuilder("""
                SELECT COUNT(*)
                FROM %s u
                JOIN %s c ON c.id = u.carrier_id
                JOIN %s r ON c.id = r.carrier_id
                WHERE 1=1
                """.formatted(TABLE_NAME, CarrierDao.TABLE_NAME, RouteDao.TABLE_NAME))
                .addFilter("u.id = :userId", "userId", userId)
                .addFilter("r.id = :routeId", "routeId", routeId);

        Integer count = namedParameterJdbcTemplate.queryForObject(
                builder.getSql(),
                builder.getParams(),
                Integer.class
        );
        return count != null && count > 0;
    }
}