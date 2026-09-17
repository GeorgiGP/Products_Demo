CREATE TABLE IF NOT EXISTS products (
    id          BIGSERIAL      PRIMARY KEY,
    name        VARCHAR(255)   NOT NULL,
    price       NUMERIC(12, 2) NOT NULL CHECK (price > 0),
    category    VARCHAR(128)   NOT NULL,
    quantity    INTEGER        NOT NULL CHECK (quantity >= 0),
    description VARCHAR(2000),

    created_at  TIMESTAMPTZ    NOT NULL,
    updated_at  TIMESTAMPTZ    NOT NULL
);

