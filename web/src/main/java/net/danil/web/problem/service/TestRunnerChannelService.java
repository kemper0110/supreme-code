package net.danil.web.problem.service;

import lombok.RequiredArgsConstructor;
import net.danil.web.problem.dto.TestResult;
import net.danil.web.problem.model.SolutionResult;
import net.danil.web.problem.repository.SolutionRepository;
import net.danil.web.problem.repository.SolutionResultRepository;
import net.danil.web.statistics.StatisticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class TestRunnerChannelService {
    public static final String TOPIC_NAME = "test-result-topic";

    private final ConcurrentHashMap<String, MonoSink<Message<?>>> messageHandlers = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(TestRunnerChannelService.class);
    private final SolutionResultRepository solutionResultRepository;
    private final SolutionRepository solutionRepository;
    private final TestResultAnalyzerService testResultAnalyzerService;
    private final StatisticsRepository statisticsRepository;

    @KafkaListener(topics = TOPIC_NAME)
    protected void listen(@Payload TestResult testResult, @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String messageId) {
        if (messageId == null) {
            logger.error("received null message id");
            return;
        }
        logger.info("received result forId({}): {}", messageId, testResult);
        final var sink = messageHandlers.get(messageId);
        try {
            final var verdict = testResultAnalyzerService.judgeResults(testResult);
            final var solution = solutionRepository.findById(testResult.solutionId()).get();

            SolutionResult solutionResult = new SolutionResult(solution.getId(), testResult.tests(), testResult.failures(), testResult.errors(), testResult.statusCode(), testResult.time(),
                    testResult.logs(), testResult.xml(), verdict.solved(), solution);
            solutionResultRepository.save(solutionResult);
            solution.setSolutionResult(solutionResult);
            statisticsRepository.updateUserStatistics(solution.getUser().getId());

            sink.success(new GenericMessage<>(solution));
        } catch (Exception e) {
            sink.error(new RuntimeException(testResult.toString(), e));
        }
    }

    public Mono<Message<?>> subscribe(String id) {
        return Mono.create(sink -> messageHandlers.put(id, sink));
    }
}
