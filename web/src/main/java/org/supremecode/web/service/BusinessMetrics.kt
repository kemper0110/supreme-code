package org.supremecode.web.service

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Service
import org.supremecode.web.repository.SolutionResultRepository
import org.supremecode.web.repository.UserRepository
import java.util.concurrent.TimeUnit
import java.util.function.ToDoubleFunction

@Service
class BusinessMetrics(
    private val meterRegistry: MeterRegistry,
    userRepository: UserRepository,
    solutionResultRepository: SolutionResultRepository
) {

    init {
        Gauge.builder("supreme_code_users_total", userRepository, ToDoubleFunction<UserRepository> { repository ->
            repository.count().toDouble()
        }).register(meterRegistry)

        Gauge.builder(
            "supreme_code_solved_problems_total",
            solutionResultRepository,
            ToDoubleFunction<SolutionResultRepository> { repository ->
                repository.countSolvedUserProblemPairs().toDouble()
            }
        ).register(meterRegistry)
    }

    fun recordSubmission(language: String) {
        Counter.builder("supreme_code_submissions")
            .tag("language", language)
            .register(meterRegistry)
            .increment()
    }

    fun recordResult(language: String, solved: Boolean, status: Int) {
        Counter.builder("supreme_code_submission_results")
            .tag("language", language)
            .tag("solved", solved.toString())
            .tag("status", status.toString())
            .register(meterRegistry)
            .increment()
    }

    fun recordResultLatency(language: String, solved: Boolean, status: Int, latencyMillis: Long) {
        meterRegistry.timer(
            "supreme_code_submission_result_latency",
            "language", language,
            "solved", solved.toString(),
            "status", status.toString()
        ).record(latencyMillis, TimeUnit.MILLISECONDS)
    }
}
