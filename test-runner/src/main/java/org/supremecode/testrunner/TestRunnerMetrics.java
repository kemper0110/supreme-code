package org.supremecode.testrunner;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TestRunnerMetrics {
    private final MeterRegistry meterRegistry;

    public void recordExecution(String language, boolean solved, int statusCode, long durationMillis) {
        Counter.builder("supreme_code_test_runner_executions")
                .tag("language", language)
                .tag("solved", Boolean.toString(solved))
                .tag("status_code", Integer.toString(statusCode))
                .register(meterRegistry)
                .increment();

        meterRegistry.timer(
                "supreme_code_test_runner_execution",
                "language", language,
                "solved", Boolean.toString(solved),
                "status_code", Integer.toString(statusCode)
        ).record(durationMillis, TimeUnit.MILLISECONDS);
    }

    public void recordFailure(String language, String stage) {
        Counter.builder("supreme_code_test_runner_failures")
                .tag("language", language)
                .tag("stage", stage)
                .register(meterRegistry)
                .increment();
    }
}
