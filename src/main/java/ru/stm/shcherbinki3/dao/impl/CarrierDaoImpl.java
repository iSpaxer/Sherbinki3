package ru.stm.shcherbinki3.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.stm.shcherbinki3.dao.CarrierDao;
import ru.stm.shcherbinki3.dao.UserDao;
import ru.stm.shcherbinki3.model.Carrier;
import ru.stm.shcherbinki3.model.User;
import ru.stm.shcherbinki3.model.type.RecordStatus;
import ru.stm.shcherbinki3.util.exception.BadRequestException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class CarrierDaoImpl implements CarrierDao {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    @Autowired
    public CarrierDaoImpl(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(namedParameterJdbcTemplate.getJdbcTemplate())
                .withTableName(TABLE_NAME)
                .usingGeneratedKeyColumns("id")
                .usingColumns("name", "phone");
    }

    @Override
    public Optional<Carrier> findByUserIdAndRecordStatus(Long userId, RecordStatus recordStatus) {
        String sql = """
                SELECT c.id, c.name, c.phone, c.record_status
                FROM %s c
                JOIN %s us ON us.carrier_id = c.id
                WHERE us.id = :userId AND c.record_status = :recordStatus
                """.formatted(TABLE_NAME, UserDao.TABLE_NAME);

        Map<String, Object> params = Map.of("userId", userId, "recordStatus", RecordStatus.ACTIVE.name());
        return doRequestInBd(sql, params);
    }

    @Override
    public Optional<Carrier> findByName(String name) {
        String sql = """
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
                    WHERE c.name = :name
                """.formatted(TABLE_NAME, UserDao.TABLE_NAME);
        try {
            return namedParameterJdbcTemplate.queryForObject(sql, Map.of("name", name), (rs, rowNum) -> {
                Carrier carrier = new Carrier();
                carrier.setId(rs.getLong("carrier_id"));
                carrier.setName(rs.getString("carrier_name"));
                carrier.setPhone(rs.getString("carrier_phone"));
                carrier.setDeletedDatetime(rs.getObject("deleted_datetime", LocalDateTime.class));
                carrier.setRecordStatus(RecordStatus.valueOf(rs.getString("carrier_record_status")));

                User owner = new User();
                owner.setId(rs.getLong("user_id"));
                owner.setEmail(rs.getString("user_email"));
                owner.setPassword(rs.getString("user_password"));
                owner.setName(rs.getString("user_name"));
                owner.setLastname(rs.getString("user_lastname"));
                owner.setPatronymic(rs.getString("user_patronymic"));
                owner.setRecordStatus(RecordStatus.valueOf(rs.getString("user_record_status")));

                carrier.setOwner(owner);

                return Optional.of(carrier);
            });
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
        boolean updatedUser = namedParameterJdbcTemplate.update(sql, Map.of("carrierId", carrierId, "id", userId)) > 0;

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
        String sql = """
                UPDATE %s
                SET record_status = :recordStatus,
                    deleted_datetime = :datetime
                WHERE id = (
                    SELECT carrier_id
                    FROM %s
                    WHERE id = :userId
                ) AND record_status != :excludedStatus
                """.formatted(TABLE_NAME, UserDao.TABLE_NAME);

        Map<String, Object> params = new HashMap<>();
        params.put("recordStatus", recordStatus.name());
        params.put("userId", userId);
        params.put("datetime", recordStatus.equals(RecordStatus.DELETED) ? LocalDateTime.now() : null);
        params.put("excludedStatus", recordStatus.name());

        return namedParameterJdbcTemplate.update(sql, params) > 0;
    }

    private Optional<Carrier> doRequestInBd(String sql, Map<String, Object> params) {
        try {
            return Optional.ofNullable(namedParameterJdbcTemplate.queryForObject(
                    sql,
                    params,
                    new BeanPropertyRowMapper<>(Carrier.class)
            ));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
