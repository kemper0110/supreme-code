package org.supremecode.web.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.supremecode.web.domain.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    @NotNull
    @EntityGraph(attributePaths = {"problemTags", "languages"})
    Optional<Problem> findById(@NotNull Long id);
}
