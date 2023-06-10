DROP TABLE journal;

CREATE TABLE journal
(
    ID    BIGINT auto_increment PRIMARY KEY,
    STATE INT,
    TIME  TIMESTAMP(9) WITH TIME ZONE,
    UUID  VARCHAR(36),
    MSG   VARCHAR(1000)
);