package net.danil.web.repository;

import net.danil.web.domain.ProblemLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemLanguageRepository extends JpaRepository<ProblemLanguage, Long> {
}
