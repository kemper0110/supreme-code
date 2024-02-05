create table solution_result
(
    user_id             bigint,
    problem_language_id bigint,
    result              varchar(131072) not null,
    constraint solution_result_composite_key PRIMARY KEY (user_id, problem_language_id),
    constraint solution_result_foreign_key foreign key (user_id, problem_language_id) REFERENCES solution (user_id, problem_language_id)
);