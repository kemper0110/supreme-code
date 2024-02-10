package net.danil.web.repository;

import net.danil.web.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
}
