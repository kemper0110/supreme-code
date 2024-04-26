package net.danil.web.problem.repository;

import net.danil.web.problem.model.Solution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolutionRepository extends JpaRepository<Solution, Long> {
    List<Solution> findByProblemSlugAndUserIdOrderByIdDesc(String slug, Long userId);
}
