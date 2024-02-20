package net.danil.web.repository;

import net.danil.web.model.ProblemLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProblemLanguageRepository extends JpaRepository<ProblemLanguage, Long> {
    Optional<ProblemLanguage> findByProblemId(Long problemId);
}