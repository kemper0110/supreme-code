create table users
(
    id       bigserial primary key,
    email    varchar(255) not null unique,
    username varchar(24)  not null unique,
    avatar   varchar(255)
);

CREATE TABLE language
(
    id           BIGSERIAL PRIMARY KEY,
    name         varchar(24)  NOT NULL,
    image        varchar(255) NOT NULL,
    pod_manifest varchar      NOT NULL
);

CREATE TABLE problem
(
    id          BIGSERIAL PRIMARY KEY,
    author_id   BIGINT         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    name        varchar(50)    NOT NULL,
    description varchar(10000) NOT NULL,
    difficulty  varchar(16)    NOT NULL
);

create table if not exists tag
(
    id   bigserial primary key,
    name varchar(24) not null
);

CREATE TABLE problem_tags
(
    problem_id BIGINT NOT NULL REFERENCES problem (id) ON DELETE CASCADE,
    tag_id     BIGINT NOT NULL REFERENCES tag (id) ON DELETE CASCADE,
    PRIMARY KEY (problem_id, tag_id)
);

CREATE TABLE problem_language
(
    id               BIGSERIAL PRIMARY KEY,
    problem_id       BIGINT          NOT NULL REFERENCES problem (id) ON DELETE CASCADE,
    language_id      BIGINT          NOT NULL REFERENCES language (id) ON DELETE CASCADE,
    initial_solution varchar(8192)   NOT NULL,
    preloaded        varchar(100000) NOT NULL,
    tests            varchar(200000) NOT NULL
);

CREATE TABLE solution
(
    id                  BIGSERIAL PRIMARY KEY,
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id             BIGINT         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    problem_language_id BIGINT         NOT NULL REFERENCES problem_language (id) ON DELETE CASCADE,
    code                varchar(20000) NOT NULL
);

CREATE TABLE solution_result
(
    id         BIGINT PRIMARY KEY REFERENCES solution (id) ON DELETE CASCADE,
    created_at TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    exit_code  INT            NOT NULL DEFAULT 0,
    stdout     varchar(10000) NOT NULL DEFAULT '',
    stderr     varchar(10000) NOT NULL DEFAULT '',
    time       FLOAT          NOT NULL DEFAULT 0,
    timed_out  BOOLEAN        NOT NULL DEFAULT FALSE,
    solved     BOOLEAN        NOT NULL DEFAULT FALSE,
    tests      varchar(10000) NOT NULL
);