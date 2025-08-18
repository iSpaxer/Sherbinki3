package ru.stm.shcherbinki3.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.stm.shcherbinki3.dao.CarrierDao;
import ru.stm.shcherbinki3.dao.RouteDao;
import ru.stm.shcherbinki3.model.Carrier;
import ru.stm.shcherbinki3.model.Route;
import ru.stm.shcherbinki3.model.type.RecordStatus;
import ru.stm.shcherbinki3.util.pagination.Pageable;

import java.util.List;
import java.util.Set;

@Repository
public class RouteDaoImpl implements RouteDao {

    private static final Set<String> ALLOWED_SORT_COLUMNS = Set.of("id", "departure", "destination", "duration_minutes");

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
                    WHERE u.id = :user_id
                    RETURNING id
                """.formatted(TABLE_NAME);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("user_id", userId)
                .addValue("departure", entity.getDeparture())
                .addValue("destination", entity.getDestination())
                .addValue("duration_minutes", entity.getDurationMinutes());

        Long id = namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
        entity.setId(id);

        return entity;
    }

    @Override
    public List<Route> findByParameters(String carrierName, String departure, String destination, Pageable pageable) {
        String sortBy = getSortBy(pageable);

        StringBuilder sql = new StringBuilder("""
        SELECT
            r.id               AS route_id,
            r.departure        AS route_departure,
            r.destination      AS route_destination,
            r.duration_minutes AS route_duration_minutes,
            r.record_status    AS route_record_status,
            
            c.id               AS carrier_id,
            c.name             AS carrier_name,
            c.phone            AS carrier_phone,
            c.record_status    AS carrier_record_status,
            c.deleted_datetime AS carrier_deleted_datetime
        FROM %s r
        JOIN %s c ON c.id = r.carrier_id
        WHERE 1=1
        """.formatted(TABLE_NAME, CarrierDao.TABLE_NAME));

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (carrierName != null && !carrierName.isBlank()) {
            sql.append(" AND c.name = :carrierName");
            params.addValue("carrierName", carrierName);
        }

        if (departure != null && !departure.isBlank()) {
            sql.append(" AND r.departure = :departure");
            params.addValue("departure", departure);
        }

        if (destination != null && !destination.isBlank()) {
            sql.append(" AND r.destination = :destination");
            params.addValue("destination", destination);
        }

        sql.append(" ORDER BY r.%s %s LIMIT :limit OFFSET :offset"
                           .formatted(sortBy, pageable.direction().name()));

        params.addValue("limit", pageable.size());
        params.addValue("offset", pageable.offset());

        System.out.println(sql);

        return namedParameterJdbcTemplate.query(sql.toString(), params, (rs, rowNum) -> {
            Carrier carrier = new Carrier();
            carrier.setId(rs.getLong("carrier_id"));
            carrier.setName(rs.getString("carrier_name"));
            carrier.setPhone(rs.getString("carrier_phone"));
            carrier.setRecordStatus(RecordStatus.valueOf(rs.getString("carrier_record_status")));
            carrier.setDeletedDatetime(
                    rs.getTimestamp("carrier_deleted_datetime") != null
                            ? rs.getTimestamp("carrier_deleted_datetime").toLocalDateTime()
                            : null
            );

            Route route = new Route();
            route.setId(rs.getLong("route_id"));
            route.setDeparture(rs.getString("route_departure"));
            route.setDestination(rs.getString("route_destination"));
            route.setDurationMinutes(rs.getLong("route_duration_minutes"));
            route.setRecordStatus(RecordStatus.valueOf(rs.getString("route_record_status")));
            route.setCarrier(carrier);

            return route;
        });
    }


    private String getSortBy(Pageable pageable) {
        String sortBy = ALLOWED_SORT_COLUMNS.contains(pageable.sortBy())
                ? pageable.sortBy()
                : "id";
        return sortBy;
    }

    @Override
    public long countByParameters(String carrierName, String departure, String destination) {
        StringBuilder sql = new StringBuilder("""
        SELECT COUNT(*)
        FROM %s r
        JOIN %s c ON c.id = r.carrier_id
        WHERE 1=1
        """.formatted(TABLE_NAME, CarrierDao.TABLE_NAME));

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (carrierName != null && !carrierName.isBlank()) {
            sql.append(" AND c.name = :carrierName");
            params.addValue("carrierName", carrierName);
        }

        if (departure != null && !departure.isBlank()) {
            sql.append(" AND r.departure = :departure");
            params.addValue("departure", departure);
        }

        if (destination != null && !destination.isBlank()) {
            sql.append(" AND r.destination = :destination");
            params.addValue("destination", destination);
        }

        return namedParameterJdbcTemplate.queryForObject(sql.toString(), params, Long.class);
    }



}



