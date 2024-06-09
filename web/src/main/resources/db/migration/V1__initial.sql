create table users
(
    id       bigserial primary key,
    username varchar(50) not null unique,
    password varchar(50) not null,
    image    varchar(255)
);

CREATE TYPE language_enum AS ENUM ('Cpp', 'Java', 'Javascript');
CREATE TYPE difficulty_enum AS ENUM ('Easy', 'Normal', 'Hard');

CREATE TABLE IF NOT EXISTS problem
(
    problem_slug VARCHAR(50) primary key,
    name         VARCHAR(255)    NOT NULL,
    description  TEXT            NOT NULL,
    difficulty   difficulty_enum NOT NULL,
    languages    language_enum[] NOT NULL
);

create table solution
(
    id           bigserial primary key,
    created_at   timestamp     not null,
    problem_slug varchar(50)   not null,
    language     language_enum not null,
    user_id      bigint        not null references users (id),
    code         text          not null
);

create table solution_result
(
    solution_id bigint primary key references solution (id),
    created_at  timestamp not null,
    tests       int       not null,
    failures    int       not null,
    errors      int       not null,
    status_code int       not null,
    time        float     not null,
    logs        text      not null,
    junit_xml   text,
    solved      boolean   not null
);
