package net.danil.web.repository;

import net.danil.web.domain.SolutionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolutionResultRepository extends JpaRepository<SolutionResult, Long> {
}
