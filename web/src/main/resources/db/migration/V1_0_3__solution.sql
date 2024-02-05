create table solution
(
    user_id             bigint,
    problem_language_id bigint,
    code                varchar(65536) not null,
    constraint solution_composite_key PRIMARY KEY (user_id, problem_language_id),
    constraint solution_foreign_user foreign key (user_id) references users(id),
    constraint solution_foreign_problem_language foreign key (problem_language_id) references problem_language(id)
);