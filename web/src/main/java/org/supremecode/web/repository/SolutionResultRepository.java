package org.supremecode.web.repository;

import org.supremecode.web.domain.SolutionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SolutionResultRepository extends JpaRepository<SolutionResult, Long> {
    @Query(
            value = """
                    select count(*)
                    from (
                        select distinct s.author_id, pl.problem_id
                        from supreme_code.solution_result sr
                        join supreme_code.solution s on s.id = sr.id
                        join supreme_code.problem_language pl on pl.id = s.problem_language_id
                        where sr.solved = true
                    ) solved_pairs
                    """,
            nativeQuery = true
    )
    long countSolvedUserProblemPairs();
}
