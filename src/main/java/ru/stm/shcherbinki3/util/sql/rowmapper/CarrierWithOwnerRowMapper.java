package ru.stm.shcherbinki3.util.sql.rowmapper;

import org.springframework.jdbc.core.RowMapper;
import ru.stm.shcherbinki3.model.Carrier;
import ru.stm.shcherbinki3.model.User;
import ru.stm.shcherbinki3.model.type.RecordStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class CarrierWithOwnerRowMapper implements RowMapper<Carrier> {
    @Override
    public Carrier mapRow(ResultSet rs, int rowNum) throws SQLException {
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

        return carrier;
    }
}
