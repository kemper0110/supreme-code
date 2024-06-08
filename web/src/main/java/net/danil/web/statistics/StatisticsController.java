package net.danil.web.statistics;

import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

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
    ResponseEntity<Statistics> get(@AuthenticationPrincipal(expression = "id") Long userId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.MINUTES))
                .body(new Statistics(
                        statisticsService.getGeneralStatistics(),
                        statisticsService.getPersonalStatistics(userId)
                ));
    }
}
