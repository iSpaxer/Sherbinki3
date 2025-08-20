package ru.stm.shcherbinki3.util.sql.rowmapper;

import org.springframework.jdbc.core.ResultSetExtractor;
import ru.stm.shcherbinki3.model.Carrier;
import ru.stm.shcherbinki3.model.Route;
import ru.stm.shcherbinki3.model.type.RecordStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class CarrierWithRoutesExtractor implements ResultSetExtractor<Carrier> {
    @Override
    public Carrier extractData(ResultSet rs) throws SQLException {
        Carrier carrier = null;

        while (rs.next()) {
            if (carrier == null) {
                carrier = new Carrier();
                carrier.setId(rs.getLong("carrier_id"));
                carrier.setName(rs.getString("name"));
                carrier.setPhone(rs.getString("phone"));
                carrier.setRecordStatus(RecordStatus.valueOf(rs.getString("carrier_record_status")));
                carrier.setRouteList(new ArrayList<>());
            }

            Long routeId = rs.getLong("route_id");
            if (!rs.wasNull()) {
                Route route = new Route();
                route.setId(routeId);
                route.setCarrier(carrier);
                route.setDeparture(rs.getString("departure"));
                route.setDestination(rs.getString("destination"));
                route.setDurationMinutes(rs.getLong("duration_minutes"));
                String routeStatus = rs.getString("route_record_status");
                route.setRecordStatus(routeStatus != null ? RecordStatus.valueOf(routeStatus) : null);
                carrier.getRouteList().add(route);
            }
        }

        return carrier;
    }
}
