package ru.stm.shcherbinki3.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.stm.shcherbinki3.dao.TicketDao;
import ru.stm.shcherbinki3.model.Ticket;
import ru.stm.shcherbinki3.model.User;
import ru.stm.shcherbinki3.util.pagination.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class TicketDaoImpl implements TicketDao {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public TicketDaoImpl(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate) {this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;}


    @Override
    public List<Ticket> findAllByUserId(Long userId) {
        return null;
    }

    @Override
    public void createAll(Long routeId, List<Ticket> ticketList) {
        String sql = """
                INSERT INTO %s (route_id, place_number, departure_datetime, price)
                VALUES (:routeId, :placeNumber, :departureDatetime, :price)
                """.formatted(TABLE_NAME);


        MapSqlParameterSource[] batchParams = ticketList.stream()
                .map(ticket -> new MapSqlParameterSource()
                        .addValue("routeId", routeId)
                        .addValue("placeNumber", ticket.getPlaceNumber())
                        .addValue("departureDatetime", ticket.getDepartureDatetime())
                        .addValue("price", ticket.getPrice())
                )
                .toArray(MapSqlParameterSource[]::new);

        namedParameterJdbcTemplate.batchUpdate(sql, batchParams);
    }
    @Override
    public List<Ticket> findAllByRouteId(Long routeId, LocalDate date, Pageable pageable) {

        StringBuilder sql = new StringBuilder("""
            SELECT
                id,
                route_id AS routeId,
                place_number AS placeNumber,
                departure_datetime AS departureDatetime,
                user_id AS userId,
                price
            FROM %s t
            WHERE t.route_id = :routeId
            """.formatted(TicketDao.TABLE_NAME));

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("routeId", routeId);

        if (date != null) {
            sql.append(" AND t.departure_datetime >= :date AND t.departure_datetime < :endDate");
            params.addValue("date", date.atStartOfDay());
            params.addValue("endDate", date.plusDays(1).atStartOfDay());
        } else {
            sql.append(" AND t.departure_datetime >= :currentTime");
            params.addValue("currentTime", LocalDateTime.now());
        }

        String sortBy = getSortBy(pageable);
        sql.append(" ORDER BY t.%s %s LIMIT :limit OFFSET :offset"
                           .formatted(sortBy, pageable.direction().name()));

        params.addValue("limit", pageable.size());
        params.addValue("offset", pageable.offset());

        return namedParameterJdbcTemplate.query(sql.toString(), params, (rs, rowNum) -> {
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
    public long countByParameters(Long routeId, LocalDate date) {
        StringBuilder sql = new StringBuilder(
                """
                SELECT COUNT(DISTINCT t.id)
                FROM %s t
                WHERE t.route_id = :routeId
                """.formatted(TABLE_NAME));

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("routeId", routeId);

        if (date != null) {
            sql.append(" AND t.departure_datetime = :date");
            params.addValue("date", date.atStartOfDay());
        } else {
            sql.append(" AND t.departure_datetime >= :currentTime");
            params.addValue("currentTime", LocalDateTime.now());
        }

        return namedParameterJdbcTemplate.queryForObject(sql.toString(), params, Long.class);
    }

    private String getSortBy(Pageable pageable) {
        return ALLOWED_SORT_COLUMNS.contains(pageable.sortBy())
                ? pageable.sortBy()
                : "id";
    }
}



