CREATE TABLE IF NOT EXISTS route (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    carrier_id BIGINT NOT NULL,
    departure VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    duration_minutes BIGINT NOT NULL,
    CONSTRAINT fk_route_carrier FOREIGN KEY(carrier_id) REFERENCES carrier(id)
);
