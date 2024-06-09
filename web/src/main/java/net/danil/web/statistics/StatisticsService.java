package net.danil.web.statistics;

import lombok.RequiredArgsConstructor;
import net.danil.web.statistics.dto.*;
import org.danil.ProblemRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StatisticsRepository statisticsRepository;

    public List<ProblemCount> topSolved() {
        return statisticsRepository.getTopSolved();
    }

    public List<ProblemCount> topAttempted() {
        return statisticsRepository.getTopAttempted();
    }

    public List<ProblemCount> topAttemptedNotSolved() {
        return statisticsRepository.getTopAttemptedNotSolved();
    }

    public DifficultyCount difficultyCounts() {
        return statisticsRepository.getDifficultyCounts();
    }

    public LanguageCount languageCounts() {
        return statisticsRepository.getLanguageCounts();
    }


    public record General(
            List<ProblemCount> topSolved,
            List<ProblemCount> topAttempted,
            List<ProblemCount> topAttemptedNotSolved,
            DifficultyCount difficultyCounts,
            LanguageCount languageCounts
    ) {

    }

    @Cacheable(value = "statistics/general", cacheManager = "generalStatisticsCacheManager")
    public General getGeneralStatistics() {
        return new General(
                topSolved(),
                topAttempted(),
                topAttemptedNotSolved(),
                difficultyCounts(),
                languageCounts()
        );
    }


    public DifficultyCount difficultyCounts(Long userId) {
        return statisticsRepository.getDifficultyCountsByUser(userId);
    }

    public LanguageCount languageCounts(Long userId) {
        return statisticsRepository.getLanguageCounts(userId);
    }

    public SolvedAttemptedCounts solvedAndAttempted(Long userId) {
        return statisticsRepository.getSolvedAndAttempted(userId);
    }

    public record Personal(
            DifficultyCount difficultyCounts,
            LanguageCount languageCounts,
            SolvedAttemptedCounts solvedAndAttempted
    ) {

    }

    @Cacheable(value = "statistics/personal", cacheManager = "personalStatisticsCacheManager")
    public Personal getPersonalStatistics(Long userId) {
        return new Personal(
                difficultyCounts(userId),
                languageCounts(userId),
                solvedAndAttempted(userId)
        );
    }
}
