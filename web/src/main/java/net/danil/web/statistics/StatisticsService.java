package net.danil.web.statistics;

import lombok.RequiredArgsConstructor;
import net.danil.web.statistics.dto.DifficultyCounts;
import net.danil.web.statistics.dto.LanguageCount;
import net.danil.web.statistics.dto.ProblemCount;
import net.danil.web.statistics.dto.SolvedAttemptedCounts;
import org.danil.ProblemRepository;
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


// private


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
}
