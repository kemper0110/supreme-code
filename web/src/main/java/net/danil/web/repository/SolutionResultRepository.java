package net.danil.web.repository;

import net.danil.web.model.SolutionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolutionResultRepository {
    SolutionResult save(SolutionResult solutionResult);
}
