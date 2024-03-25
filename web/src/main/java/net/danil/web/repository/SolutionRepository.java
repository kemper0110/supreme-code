package net.danil.web.repository;

import net.danil.web.model.Solution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolutionRepository {
    List<Solution> findByProblemSlugAndUserIdOrderByIdDesc(String slug, Long userId);
    Solution save(Solution solution);
    Optional<Solution> findById(Long id);
}
