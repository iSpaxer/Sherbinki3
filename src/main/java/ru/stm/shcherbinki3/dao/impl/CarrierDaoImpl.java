package ru.stm.shcherbinki3.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.stm.shcherbinki3.dao.CarrierDao;
import ru.stm.shcherbinki3.dao.RouteDao;
import ru.stm.shcherbinki3.dao.UserDao;
import ru.stm.shcherbinki3.model.Carrier;
import ru.stm.shcherbinki3.model.type.RecordStatus;
import ru.stm.shcherbinki3.util.exception.BadRequestException;
import ru.stm.shcherbinki3.util.sql.SqlQueryBuilder;
import ru.stm.shcherbinki3.util.sql.rowmapper.CarrierWithOwnerRowMapper;
import ru.stm.shcherbinki3.util.sql.rowmapper.CarrierWithRoutesExtractor;


import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Repository
public class CarrierDaoImpl implements CarrierDao {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    @Autowired
    public CarrierDaoImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(namedParameterJdbcTemplate.getJdbcTemplate())
                .withTableName(TABLE_NAME)
                .usingGeneratedKeyColumns("id")
                .usingColumns("name", "phone");
    }

    @Override
    public Optional<Carrier> findByUserIdAndRecordStatus(Long userId, RecordStatus recordStatus) {
        SqlQueryBuilder builder = new SqlQueryBuilder("""
                SELECT c.id, c.name, c.phone, c.record_status
                FROM %s c
                JOIN %s us ON us.carrier_id = c.id
                WHERE 1=1
                """.formatted(TABLE_NAME, UserDao.TABLE_NAME))
                .addFilter("us.id = :userId", "userId", userId)
                .addFilter("c.record_status = :recordStatus", "recordStatus", recordStatus.name());

        try {
            Carrier carrier = namedParameterJdbcTemplate.queryForObject(
                    builder.getSql(),
                    builder.getParams(),
                    new BeanPropertyRowMapper<>(Carrier.class)
            );
            return Optional.ofNullable(carrier);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Carrier> findWithRoutesByUserIdAndRecordStatus(Long userId, RecordStatus recordStatus) {
        SqlQueryBuilder builder = new SqlQueryBuilder("""
                SELECT c.id AS carrier_id, c.name, c.phone, c.record_status AS carrier_record_status,
                       r.id AS route_id, r.departure, r.destination, r.duration_minutes, r.record_status AS route_record_status
                FROM %s c
                JOIN %s us ON us.carrier_id = c.id
                JOIN %s r ON r.carrier_id = c.id
                WHERE 1=1
                """.formatted(TABLE_NAME, UserDao.TABLE_NAME, RouteDao.TABLE_NAME))
                .addFilter("us.id = :userId", "userId", userId)
                .addFilter("c.record_status = :recordStatus", "recordStatus", recordStatus.name());

        Carrier carrier = namedParameterJdbcTemplate.query(builder.getSql(), builder.getParams(), new CarrierWithRoutesExtractor());
        return Optional.ofNullable(carrier);
    }

    @Override
    public Optional<Carrier> findByName(String name) {
        SqlQueryBuilder builder = new SqlQueryBuilder("""
                SELECT
                    c.id AS carrier_id,
                    c.name AS carrier_name,
                    c.phone AS carrier_phone,
                    c.deleted_datetime AS deleted_datetime,
                    c.record_status AS carrier_record_status,
                    u.id AS user_id,
                    u.email AS user_email,
                    u.password AS user_password,
                    u.name AS user_name,
                    u.lastname AS user_lastname,
                    u.patronymic AS user_patronymic,
                    u.record_status AS user_record_status
                FROM %s c
                JOIN %s u ON u.carrier_id = c.id
                WHERE 1=1
                """.formatted(TABLE_NAME, UserDao.TABLE_NAME))
                .addFilter("c.name = :name", "name", name);

        try {
            Carrier carrier = namedParameterJdbcTemplate.queryForObject(
                    builder.getSql(),
                    builder.getParams(),
                    new CarrierWithOwnerRowMapper()
            );
            return Optional.ofNullable(carrier);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Carrier create(Carrier carrier, Long userId) {
        SqlParameterSource params = new BeanPropertySqlParameterSource(carrier);
        Number carrierId = simpleJdbcInsert.executeAndReturnKey(params);
        carrier.setId(carrierId.longValue());

        String sql = "UPDATE %s SET carrier_id = :carrierId WHERE id = :id".formatted(UserDao.TABLE_NAME);
        boolean updatedUser = namedParameterJdbcTemplate.update(
                sql,
                Map.of("carrierId", carrierId, "id", userId)
        ) > 0;

        if (!updatedUser) {
            throw new BadRequestException("User not found or could not set carrier");
        }
        return carrier;
    }

    @Override
    public boolean hardDelete(Long userId) {
        String clearCarrierSql = """
                UPDATE %s
                SET carrier_id = NULL
                WHERE id = :userId
                """.formatted(UserDao.TABLE_NAME);
        int updatedUserRows = namedParameterJdbcTemplate.update(clearCarrierSql, Map.of("userId", userId));

        String deleteCarrierSql = """
                DELETE FROM %s
                WHERE id = (
                    SELECT carrier_id
                    FROM %s
                    WHERE id = :userId
                )
                """.formatted(TABLE_NAME, UserDao.TABLE_NAME);
        int deletedCarrierRows = namedParameterJdbcTemplate.update(deleteCarrierSql, Map.of("userId", userId));

        return updatedUserRows > 0 && deletedCarrierRows > 0;
    }

    @Override
    public boolean setDeleted(Long userId, RecordStatus recordStatus) {
        SqlQueryBuilder builder = new SqlQueryBuilder("""
                UPDATE %s
                SET record_status = :recordStatus,
                    deleted_datetime = :datetime
                WHERE id = (
                    SELECT carrier_id
                    FROM %s
                    WHERE id = :userId
                )
                """.formatted(TABLE_NAME, UserDao.TABLE_NAME))
                .addFilter("record_status != :excludedStatus", "excludedStatus", recordStatus.name());

        MapSqlParameterSource params = builder.getParams();
        params.addValue("recordStatus", recordStatus.name());
        params.addValue("userId", userId);
        params.addValue("datetime", recordStatus.equals(RecordStatus.DELETED) ? LocalDateTime.now() : null);

        return namedParameterJdbcTemplate.update(builder.getSql(), params) > 0;
    }
}