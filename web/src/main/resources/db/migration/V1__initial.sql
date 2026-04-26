create table users
(
    id       bigserial primary key,
    email    varchar(255) not null unique,
    username varchar(24)  not null unique,
    password varchar(50)  not null,
    avatar   varchar(255)
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
    name varchar(32) not null
);

CREATE TABLE problem_tags
(
    problem_id BIGINT NOT NULL REFERENCES problem (id) ON DELETE CASCADE,
    tag_id     BIGINT NOT NULL REFERENCES tag (id) ON DELETE CASCADE,
    PRIMARY KEY (problem_id, tag_id)
);

CREATE TABLE problem_language
(
    id          BIGSERIAL PRIMARY KEY,
    problem_id  BIGINT      NOT NULL REFERENCES problem (id) ON DELETE CASCADE,
    language_id varchar(32) NOT NULL,
    UNIQUE (problem_id, language_id)
);

CREATE TABLE solution
(
    id                  BIGSERIAL PRIMARY KEY,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    author_id           BIGINT    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    problem_language_id BIGINT    NOT NULL REFERENCES problem_language (id) ON DELETE CASCADE
);

CREATE TABLE solution_result
(
    id         BIGINT PRIMARY KEY REFERENCES solution (id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    exit_code  INT       NOT NULL DEFAULT 0,
    solved     BOOLEAN   NOT NULL DEFAULT FALSE,
    total      INT       NOT NULL DEFAULT 0,
    failures   INT       NOT NULL DEFAULT 0,
    errors     INT       NOT NULL DEFAULT 0
);