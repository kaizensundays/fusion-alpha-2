

select schema();

select database();


DROP TABLE journal;

CREATE TABLE journal
(
    ID    BIGINT auto_increment PRIMARY KEY,
    STATE INT,
    TIME  TIMESTAMP WITH TIME ZONE,
    UUID  VARCHAR(36),
    MSG   VARCHAR(1000)
);


truncate table journal;

select *  from journal order by id desc ;

select * from journal where time < '2024-01-10 17:00:00.000-04';

insert into journal (state, time, uuid, msg) values (0, current_timestamp(), '1234567', 'msg');

update journal set STATE=0;


