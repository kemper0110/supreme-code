create table if not exists users
(
    id bigserial primary key,
    username varchar(50) not null,
    password varchar(50) not null,
    image varchar(255) not null
);