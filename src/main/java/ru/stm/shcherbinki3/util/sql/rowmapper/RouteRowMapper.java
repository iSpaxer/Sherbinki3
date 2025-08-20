package ru.stm.shcherbinki3.util.sql.rowmapper;

import org.springframework.jdbc.core.RowMapper;
import ru.stm.shcherbinki3.model.Carrier;
import ru.stm.shcherbinki3.model.Route;
import ru.stm.shcherbinki3.model.type.RecordStatus;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RouteRowMapper implements RowMapper<Route> {
    @Override
    public Route mapRow(ResultSet rs, int rowNum) throws SQLException {
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
    }
}
