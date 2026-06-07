package org.supremecode.testrunner;

import com.github.dockerjava.api.DockerClient;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.supremecode.shared.PlatformConfig;
import org.supremecode.shared.TestCaseResult;
import org.supremecode.shared.TestMessage;
import org.supremecode.shared.TestResultMessage;
import org.supremecode.testrunner.configuration.TestRunnerProperties;
import org.supremecode.testrunner.dto.TestResult;

import java.io.ByteArrayInputStream;
import java.util.stream.Collectors;

import static org.supremecode.shared.StructuredLogging.field;
import static org.supremecode.shared.StructuredLogging.openTestMessageContext;

@Service
@Slf4j
@RequiredArgsConstructor
public class Listener {
    static String resultTopic = "test-result-topic";
    final private KafkaTemplate<String, TestResultMessage> kafka;
    final private MinioClient minioClient;
    final private LanguagePluginService languagePluginService;
    private final DockerClient dockerClient;
    private final TestRunnerProperties testRunnerProperties;
    private final PlatformConfig platformConfig;
    private final TestRunnerMetrics metrics;

    @WithSpan
    protected void handleResult(TestResult testResult, String messageId, TestMessage testMessage) {
        try (var ignored = openTestMessageContext(
                testMessage,
                messageId,
                field("status_code", testResult.getStatusCode()),
                field("solved", testResult.getSolved())
        )) {
            final var logsBytes = testResult.getLogs().getBytes();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket("solutions")
                    .object(testMessage.getSolutionFolderPath() + "logs.txt")
                    .contentType("text/plain")
                    .stream(new ByteArrayInputStream(logsBytes), logsBytes.length, -1)
                    .build());

            final var reportBytes = testResult.getReport().getBytes();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket("solutions")
                    .object(testMessage.getSolutionFolderPath() + "report.txt")
                    .contentType("text/plain")
                    .stream(new ByteArrayInputStream(reportBytes), reportBytes.length, -1)
                    .build());
            log.debug("test result artifacts uploaded");
        } catch (Exception e) {
            metrics.recordFailure(testMessage.getLanguageId(), "result_artifact_upload");
            try (var ignored = openTestMessageContext(testMessage, messageId)) {
                log.error("failed to upload test result artifacts", e);
            }
            throw new RuntimeException(e);
        }
        final var testCases = testResult.getTestCases().stream()
                .map(testCase -> new TestCaseResult(
                        testCase.name(),
                        testCase.suiteName(),
                        testCase.status() == null ? null : testCase.status().name(),
                        testCase.message(),
                        testCase.durationMs()
                ))
                .collect(Collectors.toList());
        final var trm = new TestResultMessage(
                testResult.getTotal(), testResult.getFailures(), testResult.getErrors(), testResult.getSolved(), testResult.getStatusCode(),
                testMessage.getUserId(), testMessage.getProblemId(),
                testMessage.getLanguageId(), testMessage.getSolutionId(),
                testCases
        );
        kafka.send(resultTopic, messageId, trm).whenComplete((result, exception) -> {
            try (var ignored = openTestMessageContext(
                    testMessage,
                    messageId,
                    field("status_code", testResult.getStatusCode()),
                    field("solved", testResult.getSolved())
            )) {
                if (exception != null) {
                    metrics.recordFailure(testMessage.getLanguageId(), "result_send");
                    log.error("failed to send test result message", exception);
                } else {
                    log.info("test result message sent");
                }
            }
        });
    }

    @KafkaListener(topics = "test-topic", groupId = "test-group")
    protected void listen(
            @Payload TestMessage testMessage,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String messageId
    ) throws Exception {
        final var runnerStart = System.currentTimeMillis();
        final var language = testMessage.getLanguageId();

        try (var ignored = openTestMessageContext(testMessage, messageId)) {
            log.info("test message consumed");

            final var tests = new String(minioClient.getObject(GetObjectArgs.builder()
                    .bucket("problems")
                    .object(testMessage.getProblemTestPath())
                    .build()).readAllBytes());
            final var solution = new String(minioClient.getObject(GetObjectArgs.builder()
                    .bucket("solutions")
                    .object(testMessage.getSolutionFilePath())
                    .build()).readAllBytes());
            log.debug("test artifacts loaded");

            final var languageConfig = platformConfig.getLanguages().get(language);
            if (languageConfig == null) {
                metrics.recordFailure(language, "unknown_language");
                throw new IllegalStateException("Unexpected value: " + language);
            }
            final var testerConfig = languageConfig.getTesterConfig();
            final var languageTester = languagePluginService.getLanguageTester(testerConfig.getVerdictClassName());
            if (languageTester == null) {
                metrics.recordFailure(language, "unknown_language_tester");
                throw new IllegalStateException("Unexpected value: " + language);
            }
            log.info("test execution started");
            log.debug("delegating to {}", languageTester.getClass().getName());

            final var tester = new Tester(dockerClient, testRunnerProperties.getContainer().getTtk(), languageTester, testerConfig);
            tester.test(tests, solution)
                    .thenAccept(Context.current().wrapConsumer(testResult -> {
                        final var durationMillis = System.currentTimeMillis() - runnerStart;
                        try (var callbackMdc = openTestMessageContext(
                                testMessage,
                                messageId,
                                field("duration_ms", durationMillis),
                                field("status_code", testResult.getStatusCode()),
                                field("solved", testResult.getSolved())
                        )) {
                            handleResult(testResult, messageId, testMessage);
                            metrics.recordExecution(
                                    language,
                                    testResult.getSolved(),
                                    testResult.getStatusCode(),
                                    durationMillis
                            );
                            log.info("test execution finished");
                        } catch (RuntimeException exception) {
                            try (var callbackMdc = openTestMessageContext(
                                    testMessage,
                                    messageId,
                                    field("duration_ms", durationMillis),
                                    field("status_code", testResult.getStatusCode()),
                                    field("solved", testResult.getSolved())
                            )) {
                                log.error("failed to handle test result", exception);
                            }
                        }
                    }))
                    .exceptionally(exception -> {
                        metrics.recordFailure(language, "docker_execution");
                        final var durationMillis = System.currentTimeMillis() - runnerStart;
                        try (var callbackMdc = openTestMessageContext(testMessage, messageId, field("duration_ms", durationMillis))) {
                            log.error("failed to execute tests", exception);
                        }
                        return null;
                    });
        } catch (Exception exception) {
            metrics.recordFailure(language, "listen");
            try (var ignored = openTestMessageContext(testMessage, messageId)) {
                log.error("failed to consume test message", exception);
            }
            throw exception;
        }
    }
}
