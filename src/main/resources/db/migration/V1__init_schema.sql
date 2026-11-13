CREATE TABLE orders
(
    id         BIGSERIAL PRIMARY KEY,
    sku        VARCHAR(255) NOT NULL,
    quantity   INTEGER      NOT NULL,
    status     VARCHAR(50)  NOT NULL,
    created_at TIMESTAMP    NOT NULL
);

CREATE TABLE processed_events
(
    event_id     VARCHAR(255) PRIMARY KEY,
    processed_at TIMESTAMP NOT NULL
);