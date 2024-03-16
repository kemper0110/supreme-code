create table users
(
    id       bigserial primary key,
    username varchar(50) not null unique,
    password varchar(50) not null,
    image    varchar(255)
);

create table solution
(
    id           bigserial primary key,
    problem_slug varchar(50) not null,
    language     varchar(20) not null,
    user_id      bigint      not null references users (id),
    code         text        not null
);

create table solution_result
(
    solution_id bigint primary key references solution (id),
    tests       int     not null,
    failures    int     not null,
    errors      int     not null,
    status_code int     not null,
    time        float   not null,
    logs        text    not null,
    junit_xml   text,
    solved      boolean not null
);
