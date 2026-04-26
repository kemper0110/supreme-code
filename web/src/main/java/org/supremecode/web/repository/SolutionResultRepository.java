package org.supremecode.web.repository;

import org.supremecode.web.domain.SolutionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolutionResultRepository extends JpaRepository<SolutionResult, Long> {
}
