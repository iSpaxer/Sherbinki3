CREATE TABLE IF NOT EXISTS ticket (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    route_id BIGINT NOT NULL,
    user_id BIGINT,
    seat_number INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    travel_date TIMESTAMP NOT NULL,
    CONSTRAINT fk_ticket_route FOREIGN KEY(route_id) REFERENCES route(id),
    CONSTRAINT fk_ticket_user FOREIGN KEY(user_id) REFERENCES app_user(id)
);