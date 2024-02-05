create table problem_language
(
    problem_id bigint         not null,
    user_id    bigint         not null,
    language   varchar(20)    not null,
    template   varchar(2048)  not null,
    test       varchar(65536) not null,
    constraint problem_language_composite_key primary key (problem_id, user_id)
);