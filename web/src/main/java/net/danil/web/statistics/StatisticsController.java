package net.danil.web.statistics;

import lombok.RequiredArgsConstructor;
import net.danil.web.statistics.dto.DifficultyCounts;
import net.danil.web.statistics.dto.LanguageCount;
import net.danil.web.statistics.dto.ProblemCount;
import net.danil.web.statistics.dto.SolvedAttemptedCounts;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public record Statistics(
            General general,
            Personal personal
    ) {

    }

    public record General(
            List<ProblemCount> topSolved,
            List<ProblemCount> topAttempted,
            List<ProblemCount> topAttemptedNotSolved,
            DifficultyCounts difficultyCounts,
            List<LanguageCount> languageCounts
    ) {

    }

    public record Personal(
            DifficultyCounts difficultyCounts,
            List<LanguageCount> languageCounts,
            SolvedAttemptedCounts solvedAndAttempted
    ) {

    }


    @GetMapping
    Statistics get(@AuthenticationPrincipal(expression = "id") Long userId) {
        return new Statistics(
                new General(
                        statisticsService.topSolved(),
                        statisticsService.topAttempted(),
                        statisticsService.topAttemptedNotSolved(),
                        statisticsService.difficultyCounts(),
                        statisticsService.languageCounts()
                ),
                new Personal(
                        statisticsService.difficultyCounts(userId),
                        statisticsService.languageCounts(userId),
                        statisticsService.solvedAndAttempted(userId)
                )
        );
    }
}
