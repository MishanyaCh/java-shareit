DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS item_requests;

CREATE TABLE IF NOT EXISTS users
(
    id
    INTEGER
    GENERATED
    BY
    DEFAULT AS
    IDENTITY
    PRIMARY
    KEY,
    name
    VARCHAR,
    email
    VARCHAR
(
    50
),
    CONSTRAINT UNI_USER_EMAIL UNIQUE
(
    email
)
    );

CREATE TABLE IF NOT EXISTS item_requests
(
    id
    INTEGER
    GENERATED
    BY
    DEFAULT AS
    IDENTITY
    PRIMARY
    KEY,
    description
    VARCHAR
(
    255
),
    creation_date TIMESTAMP WITHOUT TIME ZONE,
    requester_id INTEGER REFERENCES users
(
    id
)
    );

CREATE TABLE IF NOT EXISTS items
(
    id
    INTEGER
    GENERATED
    BY
    DEFAULT AS
    IDENTITY
    PRIMARY
    KEY,
    name
    VARCHAR,
    description
    VARCHAR
(
    255
),
    is_available BOOLEAN,
    owner_id INTEGER REFERENCES users
(
    id
) ON DELETE CASCADE,
    request_id INTEGER REFERENCES item_requests
(
    id
)
    );

CREATE TABLE IF NOT EXISTS bookings
(
    id
    INTEGER
    GENERATED
    BY
    DEFAULT AS
    IDENTITY
    PRIMARY
    KEY,
    start_booking_date
    TIMESTAMP
    WITHOUT
    TIME
    ZONE,
    end_booking_date
    TIMESTAMP
    WITHOUT
    TIME
    ZONE,
    booking_status
    VARCHAR
(
    20
),
    item_id INTEGER REFERENCES items
(
    id
),
    booker_id INTEGER REFERENCES users
(
    id
)
    );

CREATE TABLE IF NOT EXISTS comments
(
    id
    INTEGER
    GENERATED
    BY
    DEFAULT AS
    IDENTITY
    PRIMARY
    KEY,
    text
    VARCHAR
(
    255
),
    creation_date TIMESTAMP WITHOUT TIME ZONE,
    item_id INTEGER REFERENCES items
(
    id
),
    author_id INTEGER REFERENCES users
(
    id
)
    );