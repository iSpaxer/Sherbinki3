package ru.stm.shcherbinki3.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.stm.shcherbinki3.dao.CarrierDao;
import ru.stm.shcherbinki3.dao.RouteDao;
import ru.stm.shcherbinki3.dao.TicketDao;
import ru.stm.shcherbinki3.model.Route;
import ru.stm.shcherbinki3.util.exception.BadRequestException;
import ru.stm.shcherbinki3.util.exception.ResourceNotFoundException;
import ru.stm.shcherbinki3.util.pagination.Pageable;
import ru.stm.shcherbinki3.util.sql.SqlQueryBuilder;
import ru.stm.shcherbinki3.util.sql.rowmapper.RouteRowMapper;

import java.time.LocalDate;
import java.util.List;

@Repository
public class RouteDaoImpl implements RouteDao {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public RouteDaoImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Route create(Long userId, Route entity) {
        String sql = """
        INSERT INTO %s (carrier_id, departure, destination, duration_minutes)
        SELECT u.carrier_id, :departure, :destination, :duration_minutes
        FROM app_user u
        WHERE u.id = :user_id AND u.carrier_id IS NOT NULL
        RETURNING id
        """.formatted(TABLE_NAME);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("user_id", userId)
                .addValue("departure", entity.getDeparture())
                .addValue("destination", entity.getDestination())
                .addValue("duration_minutes", entity.getDurationMinutes());

        try {
            Long id = namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
            if (id == null) {
                throw new BadRequestException("Cannot create route: user with id " + userId + " has no associated carrier");
            }
            entity.setId(id);
            return entity;
        } catch (EmptyResultDataAccessException e) {
            throw new BadRequestException("Cannot create route: user with id " + userId + " has no associated carrier");
        }
    }

    @Override
    public List<Route> findByParameters(String carrierName, String departure, String destination, LocalDate date, Pageable pageable) {
        SqlQueryBuilder builder = new SqlQueryBuilder("""
                SELECT DISTINCT
                    r.id AS route_id,
                    r.departure AS route_departure,
                    r.destination AS route_destination,
                    r.duration_minutes AS route_duration_minutes,
                    r.record_status AS route_record_status,
                    
                    c.id AS carrier_id,
                    c.name AS carrier_name,
                    c.phone AS carrier_phone,
                    c.record_status AS carrier_record_status,
                    c.deleted_datetime AS carrier_deleted_datetime
                FROM %s r
                JOIN %s c ON c.id = r.carrier_id
                JOIN %s t ON t.route_id = r.id
                WHERE 1=1
                """.formatted(TABLE_NAME, CarrierDao.TABLE_NAME, TicketDao.TABLE_NAME))
                .addFilter("c.name LIKE :carrierName", "carrierName", "%" + (carrierName != null ? carrierName : "") + "%")
                .addFilter("r.departure LIKE :departure", "departure", "%" + (departure != null ? departure : "") + "%")
                .addFilter("r.destination LIKE :destination", "destination", "%" + (destination != null ? destination : "") + "%")
                .addOneDayDateFilter(date, "t.departure_datetime")
                .addPagination(pageable, "r", ALLOWED_SORT_COLUMNS);

        return namedParameterJdbcTemplate.query(
                builder.getSql(),
                builder.getParams(),
                new RouteRowMapper()
        );
    }

    @Override
    public void updateDurationMinutes(Long routeId, Long durationMinutes) {
        SqlQueryBuilder builder = new SqlQueryBuilder(
                """
                UPDATE %s
                SET duration_minutes = :durationMinutes
                """.formatted(TABLE_NAME))
                .addValue("durationMinutes", durationMinutes)
                .addFilterWhere("id = :routeId", "routeId", routeId);

        String sql = builder.getSql();
        MapSqlParameterSource params = builder.getParams();

        int rowsAffected = namedParameterJdbcTemplate.update(sql, params);
        if (rowsAffected == 0) {
            throw new ResourceNotFoundException("Route with id=" + routeId + " not found");
        }
    }

    @Override
    public long countByParameters(String carrierName, String departure, String destination, LocalDate date) {
        SqlQueryBuilder builder = new SqlQueryBuilder("""
                SELECT COUNT(DISTINCT r.id)
                FROM %s r
                JOIN %s c ON c.id = r.carrier_id
                JOIN %s t ON t.route_id = r.id
                WHERE 1=1
                """.formatted(TABLE_NAME, CarrierDao.TABLE_NAME, TicketDao.TABLE_NAME))
                .addFilter("c.name LIKE :carrierName", "carrierName", "%" + (carrierName != null ? carrierName : "") + "%")
                .addFilter("r.departure LIKE :departure", "departure", "%" + (departure != null ? departure : "") + "%")
                .addFilter("r.destination LIKE :destination", "destination", "%" + (destination != null ? destination : "") + "%")
                .addOneDayDateFilter(date, "t.departure_datetime");

        Long count = namedParameterJdbcTemplate.queryForObject(builder.getSql(), builder.getParams(), Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public void delete(Long routeId) {
        // Удаление связанных билетов
        SqlQueryBuilder ticketBuilder = new SqlQueryBuilder(
                """
                DELETE FROM %s
                """.formatted(TicketDao.TABLE_NAME))
                .addFilterWhere("route_id = :routeId", "routeId", routeId);

        String ticketSql = ticketBuilder.getSql();
        MapSqlParameterSource ticketParams = ticketBuilder.getParams();

        int ticketsDeleted = namedParameterJdbcTemplate.update(ticketSql, ticketParams);

        // Удаление маршрута
        SqlQueryBuilder routeBuilder = new SqlQueryBuilder(
                """
                DELETE FROM %s
                """.formatted(TABLE_NAME))
                .addFilterWhere("id = :routeId", "routeId", routeId);

        String routeSql = routeBuilder.getSql();
        MapSqlParameterSource routeParams = routeBuilder.getParams();

        int rowsAffected = namedParameterJdbcTemplate.update(routeSql, routeParams);
        if (rowsAffected == 0) {
            throw new ResourceNotFoundException("Route with id=" + routeId + " not found");
        }
    }
}