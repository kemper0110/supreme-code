create table users
(
    id       bigserial primary key,
    username varchar(50)  not null,
    password varchar(50)  not null,
    image    varchar(255)
);

create table problem
(
    id          bigserial primary key,
    name        varchar(50) not null,
    description text        not null,
    difficulty  varchar(20) not null,
    active bool not null default false
);

create table problem_language
(
    id         bigserial primary key,
    problem_id bigint      not null references problem (id) on update cascade on delete cascade,
    language   varchar(20) not null,
    template   text        not null,
    test       text        not null,
    unique (problem_id, language)
);

create table solution
(
    id                  bigserial primary key,
    problem_language_id bigint not null references problem_language (id),
    user_id             bigint not null references users (id),
    code                text   not null
);

create table solution_result
(
    solution_id bigint primary key references solution (id),
    result      text not null,
    is_build_error    bool
);