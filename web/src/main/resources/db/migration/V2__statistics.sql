-- General statistics materialized views

CREATE MATERIALIZED VIEW general_statistics_topSolved AS
select problem_slug as problemSlug, count(*) as count
from (select user_id, problem_slug, language
      from supreme_code.supreme_code.solution s
               inner join supreme_code.supreme_code.solution_result sr on s.id = sr.solution_id
      where sr.solved = true
      group by user_id, problem_slug, language) as problem_result
group by problem_slug
order by count
limit 5;

create materialized view general_statistics_topAttempted as
select problem_slug as problemSlug, count(*) as count
from (select distinct user_id, problem_slug, language
      from supreme_code.supreme_code.solution s
               inner join supreme_code.supreme_code.solution_result sr on s.id = sr.solution_id) as problem_result
group by problem_slug
order by count
limit 5;

create materialized view general_statistics_topAttemptedNotSolved as
select problem_slug as problemSlug, count(*) as count
from (select user_id, problem_slug, language, max(cast(sr.solved as int)) as solved
      from supreme_code.supreme_code.solution s
               inner join supreme_code.supreme_code.solution_result sr on s.id = sr.solution_id
      group by user_id, problem_slug, language
      having max(cast(sr.solved as int)) = 0) as problem_result
group by problem_slug
order by count
limit 5;

create materialized view general_statistics_difficultyCounts as
select coalesce(sum(case when dc.difficulty = 'Easy' then dc.count else 0 end), 0)   as Easy,
       coalesce(sum(case when dc.difficulty = 'Normal' then dc.count else 0 end), 0) as Normal,
       coalesce(sum(case when dc.difficulty = 'Hard' then dc.count else 0 end), 0)   as Hard
from (SELECT p.difficulty as difficulty, COUNT(*) AS count
      FROM (SELECT user_id, problem_slug, language, MAX(CAST(sr.solved AS INT)) AS solved
            FROM supreme_code.supreme_code.solution s
                     INNER JOIN supreme_code.supreme_code.solution_result sr ON s.id = sr.solution_id
            GROUP BY user_id, problem_slug, language
            HAVING MAX(CAST(sr.solved AS INT)) = 0) AS problem_result
               JOIN supreme_code.supreme_code.problem p ON p.problem_slug = problem_result.problem_slug
      GROUP BY p.difficulty) as dc
;

create materialized view general_statistics_languageCounts as
select coalesce(sum(case when language = 'Cpp' then count else 0 end), 0)        as Cpp,
       coalesce(sum(case when language = 'Java' then count else 0 end), 0)       as Java,
       coalesce(sum(case when language = 'Javascript' then count else 0 end), 0) as Javascript
from (select language, count(*) as count
      from (select user_id, problem_slug, max(cast(sr.solved as int)) as solved, language
            from supreme_code.supreme_code.solution s
                     inner join supreme_code.supreme_code.solution_result sr on s.id = sr.solution_id
            group by user_id, problem_slug, language) as problem_result
      group by language) as lc;


-- Personal statistics tables

create table user_statistics_difficulty
(
    user_id bigint primary key not null references users (id),
    easy    int                not null,
    normal  int                not null,
    hard    int                not null
);


create table user_statistics_language
(
    user_id    bigint primary key not null references users (id),
    Cpp        int                not null,
    Java       int                not null,
    Javascript int                not null
);


create table user_statistics_solved
(
    user_id        bigint primary key not null references users (id),
    solvedCount    int                not null,
    attemptedCount int                not null
);
