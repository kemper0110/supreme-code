create procedure updateGeneralStatistics()
    language plpgsql
as
$$
begin
    refresh materialized view supreme_code.general_statistics_topSolved;
    refresh materialized view supreme_code.general_statistics_topAttempted;
    refresh materialized view supreme_code.general_statistics_topAttemptedNotSolved;
    refresh materialized view supreme_code.general_statistics_difficultyCounts;
    refresh materialized view supreme_code.general_statistics_languageCounts;
end;
$$;

create or replace procedure updateUserStatistics(userId bigint)
    language plpgsql
as
$$
begin

    merge into supreme_code.user_statistics_difficulty as t
    using (select userId                                                                        as user_id,
                  coalesce(sum(case when dc.difficulty = 'Easy' then dc.count else 0 end), 0)   as Easy,
                  coalesce(sum(case when dc.difficulty = 'Normal' then dc.count else 0 end), 0) as Normal,
                  coalesce(sum(case when dc.difficulty = 'Hard' then dc.count else 0 end), 0)   as Hard
           from (select p.difficulty as difficulty, COUNT(*) as count
                 from (select user_id, problem_slug, max(cast(sr.solved as int)) as solved, language
                       from supreme_code.solution s
                                inner join supreme_code.solution_result sr on s.id = sr.solution_id
                       where user_id = userId
                       group by user_id, problem_slug, language) as problem_result
                          JOIN supreme_code.problem p ON p.problem_slug = problem_result.problem_slug
                 GROUP BY p.difficulty) as dc) as s
    on t.user_id = s.user_id
    when matched then
        update set Easy = s.Easy, Normal = s.Normal, Hard = s.Hard
    when not matched then
        insert (user_id, Easy, Normal, Hard) values (userId, s.Easy, s.Normal, s.Hard);


    merge into supreme_code.user_statistics_language as t
    using (select userId                                                                    as user_id,
                  coalesce(sum(case when language = 'Cpp' then count else 0 end), 0)        as Cpp,
                  coalesce(sum(case when language = 'Java' then count else 0 end), 0)       as Java,
                  coalesce(sum(case when language = 'Javascript' then count else 0 end), 0) as Javascript
           from (select language, count(*) as count
                 from (select user_id, problem_slug, max(cast(sr.solved as int)) as solved, language
                       from supreme_code.solution s
                                inner join supreme_code.solution_result sr on s.id = sr.solution_id
                       where user_id = userId
                       group by user_id, problem_slug, language) as problem_result
                 group by language) as lc
           group by user_id) as s
    on t.user_id = s.user_id
    when matched then
        update set Cpp = s.Cpp, Java = s.Java, Javascript = s.Javascript
    when not matched then
        insert (user_id, Cpp, Java, Javascript) values (userId, s.Cpp, s.Java, s.Javascript);


    merge into supreme_code.user_statistics_solved as t
    using (select userId                                                   as user_id,
                  coalesce(sum(case when solved = 1 then 1 else 0 end), 0) as solvedCount,
                  coalesce(sum(case when solved = 0 then 1 else 0 end), 0) as attemptedCount
           from (select max(cast(sr.solved as int)) as solved
                 from supreme_code.solution s
                          inner join supreme_code.solution_result sr on s.id = sr.solution_id
                 where user_id = userId
                 group by problem_slug) as solved_table
           group by user_id) as s
    on t.user_id = s.user_id
    when matched then
        update set solvedCount = s.solvedCount, attemptedCount = s.attemptedCount
    when not matched then
        insert (user_id, solvedCount, attemptedCount) values (userId, s.solvedCount, s.attemptedCount);

end
$$;