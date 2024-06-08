package net.danil.web.statistics;

import net.danil.web.problem.model.Solution;
import net.danil.web.statistics.dto.DifficultyCount;
import net.danil.web.statistics.dto.LanguageCount;
import net.danil.web.statistics.dto.ProblemCount;
import net.danil.web.statistics.dto.SolvedAttemptedCounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatisticsRepository extends JpaRepository<Solution, Long> {
    @Query(value = "select problemSlug, count from supreme_code.general_statistics_topSolved", nativeQuery = true)
    public List<ProblemCount> getTopSolved();


    @Query(value = "select problemSlug, count from supreme_code.general_statistics_topAttempted", nativeQuery = true)
    public List<ProblemCount> getTopAttempted();


    @Query(value = "select problemSlug, count from supreme_code.general_statistics_topAttemptedNotSolved", nativeQuery = true)
    public List<ProblemCount> getTopAttemptedNotSolved();


    @Query(value = "select difficulty, count from supreme_code.general_statistics_difficultyCounts", nativeQuery = true)
    public List<DifficultyCount> getDifficultyCounts();


    @Query(value = "select language, count from supreme_code.general_statistics_languageCounts", nativeQuery = true)
    public List<LanguageCount> getLanguageCounts();

    // personal

    @Query(value = "select easy, normal, hard from supreme_code.user_statistics_difficulty where user_id = ?1", nativeQuery = true)
    public List<DifficultyCount> getDifficultyCountsByUser(Long userId);


    @Query(value = "select cpp, java, javascript from supreme_code.user_statistics_language where user_id = ?1", nativeQuery = true)
    public List<LanguageCount> getLanguageCounts(Long userId);


    @Query(value = "select solvedCount, attemptedCount from supreme_code.user_statistics_solved where user_id = ?1", nativeQuery = true)
    public SolvedAttemptedCounts getSolvedAndAttempted(Long userId);

    @Procedure("supreme_code.supreme_code.updateUserStatistics")
    public void updateUserStatistics(Long userId);

    @Procedure("supreme_code.updategeneralstatistics")
    public void updateGeneralStatistics();
}
