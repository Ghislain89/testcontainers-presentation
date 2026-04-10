CREATE SEQUENCE fruit_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE fruit (
    id   BIGINT PRIMARY KEY DEFAULT nextval('fruit_seq'),
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255)
);
