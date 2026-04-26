package org.supremecode.web.repository;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.supremecode.web.domain.ProblemLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemLanguageRepository extends JpaRepository<ProblemLanguage, Long> {
    ProblemLanguage findByProblemIdAndLanguageId(Long problem_id, String languageId);
}
