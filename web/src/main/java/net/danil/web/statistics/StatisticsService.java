package net.danil.web.statistics;

import lombok.RequiredArgsConstructor;
import net.danil.web.statistics.dto.DifficultyCounts;
import net.danil.web.statistics.dto.LanguageCount;
import net.danil.web.statistics.dto.ProblemCount;
import net.danil.web.statistics.dto.SolvedAttemptedCounts;
import org.danil.ProblemRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StatisticsRepository statisticsRepository;

    private final ProblemRepository problemRepository;

    public List<ProblemCount> topSolved() {
        return statisticsRepository.getTopSolved();
    }

    public List<ProblemCount> topAttempted() {
        return statisticsRepository.getTopAttempted();
    }

    public List<ProblemCount> topAttemptedNotSolved() {
        return statisticsRepository.getTopAttemptedNotSolved();
    }

    public DifficultyCounts difficultyCounts() {
        long easy = 0, normal = 0, hard = 0;
        for (var problemCount : statisticsRepository.getDifficultyCounts()) {
            final var problem = problemRepository.getBySlug(problemCount.getProblemSlug());
            if(problem == null) continue;
            switch (problem.getDifficulty()) {
                case Easy -> ++easy;
                case Normal -> ++normal;
                case Hard -> ++hard;
            }
        }
        return new DifficultyCounts(easy, normal, hard);
    }

    public List<LanguageCount> languageCounts() {
        return statisticsRepository.getLanguageCounts();
    }


    public record General(
            List<ProblemCount> topSolved,
            List<ProblemCount> topAttempted,
            List<ProblemCount> topAttemptedNotSolved,
            DifficultyCounts difficultyCounts,
            List<LanguageCount> languageCounts
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


    public DifficultyCounts difficultyCounts(Long userId) {
        long easy = 0, normal = 0, hard = 0;
        for (var problemCount : statisticsRepository.getDifficultyCountsByUser(userId)) {
            final var problem = problemRepository.getBySlug(problemCount.getProblemSlug());
            if(problem == null) continue;
            switch (problem.getDifficulty()) {
                case Easy -> ++easy;
                case Normal -> ++normal;
                case Hard -> ++hard;
            }
        }
        return new DifficultyCounts(easy, normal, hard);
    }

    public List<LanguageCount> languageCounts(Long userId) {
        return statisticsRepository.getLanguageCounts(userId);
    }

    public SolvedAttemptedCounts solvedAndAttempted(Long userId) {
        return statisticsRepository.getSolvedAndAttempted(userId);
    }

    public record Personal(
            DifficultyCounts difficultyCounts,
            List<LanguageCount> languageCounts,
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
