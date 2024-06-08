package net.danil.web.statistics;

import net.danil.web.problem.model.Solution;
import net.danil.web.statistics.dto.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatisticsRepository extends JpaRepository<Solution, Long> {
    @Query(value = """
            select problem_slug as problemSlug, count(*) as count
                from (select user_id, problem_slug, language
                from supreme_code.supreme_code.solution s
                inner join supreme_code.supreme_code.solution_result sr on s.id = sr.solution_id
                where sr.solved = true
                group by user_id, problem_slug, language) as problem_result
                group by problem_slug order by count limit 5;""", nativeQuery = true)
    List<ProblemCount> getTopSolved();


    @Query(value = """
            select problem_slug as problemSlug, count(*) as count
                from (select distinct user_id, problem_slug, language
                from supreme_code.supreme_code.solution s
                inner join supreme_code.supreme_code.solution_result sr on s.id = sr.solution_id) as problem_result
                group by problem_slug order by count limit 5;""", nativeQuery = true)
    List<ProblemCount> getTopAttempted();


    @Query(value = """
            select problem_slug as problemSlug, count(*) as count
                from (select user_id, problem_slug, language, max(cast(sr.solved as int)) as solved
                from supreme_code.supreme_code.solution s
                inner join supreme_code.supreme_code.solution_result sr on s.id = sr.solution_id
                group by user_id, problem_slug, language having max(cast(sr.solved as int)) = 0) as problem_result
                group by problem_slug order by count limit 5;""", nativeQuery = true)
    List<ProblemCount> getTopAttemptedNotSolved();


    @Query(value = """
            SELECT p.difficulty, COUNT(*) AS count
                                              FROM (
                                                       SELECT user_id, problem_slug, language, MAX(CAST(sr.solved AS INT)) AS solved
                                                       FROM supreme_code.supreme_code.solution s
                                                                INNER JOIN supreme_code.supreme_code.solution_result sr ON s.id = sr.solution_id
                                                       GROUP BY user_id, problem_slug, language
                                                       HAVING MAX(CAST(sr.solved AS INT)) = 0
                                                   ) AS problem_result
                                                       JOIN supreme_code.supreme_code.problem p ON p.problem_slug = problem_result.problem_slug
                                              GROUP BY p.difficulty
                                              ORDER BY p.difficulty limit 5""", nativeQuery = true)
    List<DifficultyCount> getDifficultyCounts();


    @Query(value = """
            select language, count(*) as count
                from (select user_id, problem_slug, max(cast(sr.solved as int)) as solved, language
                from supreme_code.supreme_code.solution s
                inner join supreme_code.supreme_code.solution_result sr on s.id = sr.solution_id
                group by user_id, problem_slug, language) as problem_result
                group by language
            """, nativeQuery = true)
    List<LanguageCount> getLanguageCounts();

    // private

    @Query(value = """
            select  p.difficulty, COUNT(*) as count
            from (select user_id, problem_slug, max(cast(sr.solved as int)) as solved, language
                  from supreme_code.supreme_code.solution s
                           inner join supreme_code.supreme_code.solution_result sr on s.id = sr.solution_id
                  where user_id = ?1
                  group by user_id, problem_slug, language) as problem_result
                     JOIN supreme_code.supreme_code.problem p ON p.problem_slug = problem_result.problem_slug
            GROUP BY p.difficulty
            ORDER BY p.difficulty limit 5""", nativeQuery = true)
    List<DifficultyCount> getDifficultyCountsByUser(Long userId);


    @Query(value = """
            select language, count(*) as count
                from (select user_id, problem_slug, max(cast(sr.solved as int)) as solved, language
                from supreme_code.supreme_code.solution s
                inner join supreme_code.supreme_code.solution_result sr on s.id = sr.solution_id
                where user_id = ?1
                group by user_id, problem_slug, language) as problem_result
                group by language;
            """, nativeQuery = true)
    List<LanguageCount> getLanguageCounts(Long userId);


    @Query(value = """
            select
                sum(case when solved = 1 then 1 else 0 end) as solvedCount,
                sum(case when solved = 0 then 1 else 0 end) as attemptedCount
            from (
                select max(cast(sr.solved as int)) as solved
                from supreme_code.supreme_code.solution s inner join supreme_code.supreme_code.solution_result sr on s.id = sr.solution_id
                where user_id = ?1
                group by problem_slug
            ) as solved_table;
            """, nativeQuery = true)
    SolvedAttemptedCounts getSolvedAndAttempted(Long userId);
}
