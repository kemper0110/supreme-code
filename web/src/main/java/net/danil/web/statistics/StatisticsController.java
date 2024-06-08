package net.danil.web.statistics;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public record Statistics(
            StatisticsService.General general,
            StatisticsService.Personal personal
    ) {

    }


    @GetMapping
    Statistics get(@AuthenticationPrincipal(expression = "id") Long userId) {
        return new Statistics(
                statisticsService.getGeneralStatistics(),
                statisticsService.getPersonalStatistics(userId)
        );
    }
}
