package net.danil.web.problem.repository;

import net.danil.web.problem.model.SolutionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolutionResultRepository extends JpaRepository<SolutionResult, Long> {
}
