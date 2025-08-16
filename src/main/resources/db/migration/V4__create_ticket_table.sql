CREATE TABLE IF NOT EXISTS ticket (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    route_id BIGINT NOT NULL,
    place_number INT NOT NULL,
    departure_datetime TIMESTAMP NOT NULL,
    user_id BIGINT,
    price DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_ticket_route FOREIGN KEY(route_id) REFERENCES route(id),
    CONSTRAINT fk_ticket_user FOREIGN KEY(user_id) REFERENCES app_user(id),
    CONSTRAINT uq_ticket UNIQUE (route_id, place_number, departure_datetime)
);