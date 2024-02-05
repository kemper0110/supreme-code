create table problem
(
    id bigserial primary key,
    name varchar(50) not null,
    description varchar(4096) not null,
    difficulty varchar(20) not null
)