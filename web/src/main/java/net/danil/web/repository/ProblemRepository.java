package net.danil.web.repository;

import net.danil.web.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    @Query("select p from Problem p join fetch p.languages")
    Optional<Problem> findDetailedById(Long id);
}
