package ru.stm.shcherbinki3.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.stm.shcherbinki3.dao.TicketDao;
import ru.stm.shcherbinki3.model.Ticket;

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



}
