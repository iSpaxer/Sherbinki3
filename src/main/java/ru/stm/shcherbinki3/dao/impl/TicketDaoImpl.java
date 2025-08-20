package ru.stm.shcherbinki3.dao.impl;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.stm.shcherbinki3.dao.TicketDao;
import ru.stm.shcherbinki3.model.Ticket;
import ru.stm.shcherbinki3.model.User;
import ru.stm.shcherbinki3.util.pagination.Pageable;
import ru.stm.shcherbinki3.util.sql.SqlQueryBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class TicketDaoImpl implements TicketDao {


    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public TicketDaoImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public void createAll(Long routeId, List<Ticket> ticketList) {
        String sql = """
                INSERT INTO %s (route_id, place_number, departure_datetime, price)
                VALUES (:routeId, :placeNumber, :departureDatetime, :price)
                """.formatted(TABLE_NAME);

        MapSqlParameterSource[] batchParams = SqlQueryBuilder.toBatchParams(ticketList, (entity, params) -> {
            Ticket ticket = (Ticket) entity;
            params.addValue("routeId", routeId)
                    .addValue("placeNumber", ticket.getPlaceNumber())
                    .addValue("departureDatetime", ticket.getDepartureDatetime())
                    .addValue("price", ticket.getPrice());
        });

        namedParameterJdbcTemplate.batchUpdate(sql, batchParams);
    }

    @Override
    public boolean assignTicketToUser(Long userId, Long ticketId) {
        SqlQueryBuilder builder = new SqlQueryBuilder(
            """
            UPDATE %s
            SET user_id = :userId
            """.formatted(TABLE_NAME))
                .addValue("userId", userId)
                .addFilterWhere("id = :ticketId", "ticketId", ticketId);


        return namedParameterJdbcTemplate.update(builder.getSql(), builder.getParams()) > 0;
    }


    @Override
    public List<Ticket> findAllByRouteId(@NotNull Long routeId, LocalDate date, @NotNull Pageable pageable) {
        SqlQueryBuilder builder = new SqlQueryBuilder("""
                SELECT
                    id,
                    route_id AS routeId,
                    place_number AS placeNumber,
                    departure_datetime AS departureDatetime,
                    user_id AS userId,
                    price
                FROM %s t
                WHERE 1=1
                """.formatted(TABLE_NAME))
                .addFilter("t.route_id = :routeId", "routeId", routeId)
                .addOneDayDateFilter(date, "t.departure_datetime")
                .addPagination(pageable, "t", ALLOWED_SORT_COLUMNS);

        return namedParameterJdbcTemplate.query(builder.getSql(), builder.getParams(), (rs, rowNum) -> {
            Ticket ticket = new Ticket();
            ticket.setId(rs.getLong("id"));
            ticket.setPlaceNumber(rs.getInt("placeNumber"));
            ticket.setDepartureDatetime(rs.getObject("departureDatetime", LocalDateTime.class));
            ticket.setPrice(rs.getBigDecimal("price"));

            Long userId = rs.getLong("userId");
            if (!rs.wasNull()) {
                User user = new User();
                user.setId(userId);
                ticket.setUser(user);
            }

            return ticket;
        });
    }

    @Override
    public List<Ticket> findAllByUserId(@NotNull Long userId, LocalDate after, LocalDate before, Pageable pageable) {
        SqlQueryBuilder builder = new SqlQueryBuilder("""
                SELECT
                    id,
                    route_id AS routeId,
                    place_number AS placeNumber,
                    departure_datetime AS departureDatetime,
                    user_id AS userId,
                    price
                FROM %s t
                WHERE 1=1
                """.formatted(TABLE_NAME))
                .addFilter("t.user_id = :userId", "userId", userId)
                .addDateRangeFilter(after, before, "t.departure_datetime")
                .addPagination(pageable, "t", ALLOWED_SORT_COLUMNS);

        return namedParameterJdbcTemplate.query(
                builder.getSql(),
                builder.getParams(),
                new BeanPropertyRowMapper<>(Ticket.class)
        );
    }

    @Override
    public long countByParameters(@NotNull Long routeId, LocalDate date) {
        SqlQueryBuilder builder = new SqlQueryBuilder("""
                SELECT COUNT(DISTINCT t.id)
                FROM %s t
                WHERE 1=1
                """.formatted(TABLE_NAME))
                .addFilter("t.route_id = :routeId", "routeId", routeId)
                .addOneDayDateFilter(date, "t.departure_datetime");

        Long count = namedParameterJdbcTemplate.queryForObject(builder.getSql(), builder.getParams(), Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public long countByParameters(@NotNull Long userId, LocalDate after, LocalDate before, @NotNull Pageable pageable) {
        SqlQueryBuilder builder = new SqlQueryBuilder("""
                SELECT COUNT(DISTINCT t.id)
                FROM %s t
                WHERE t.user_id = :userId
                """.formatted(TABLE_NAME))
                .addFilter("t.user_id = :userId", "userId", userId)
                .addDateRangeFilter(after, before, "t.departure_datetime");

        Long count = namedParameterJdbcTemplate.queryForObject(builder.getSql(), builder.getParams(), Long.class);
        return count != null ? count : 0L;
    }
}