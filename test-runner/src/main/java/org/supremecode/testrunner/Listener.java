package org.supremecode.testrunner;

import com.github.dockerjava.api.DockerClient;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.supremecode.shared.PlatformConfig;
import org.supremecode.shared.TestMessage;
import org.supremecode.shared.TestResultMessage;
import org.supremecode.testrunner.configuration.TestRunnerProperties;
import org.supremecode.testrunner.dto.TestResult;

import java.io.ByteArrayInputStream;

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
        log.info("Tests for id {} finished", messageId);
        try {
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
        } catch (Exception e) {
            metrics.recordFailure(testMessage.getLanguageId(), "result_artifact_upload");
            throw new RuntimeException(e);
        }
        final var trm = new TestResultMessage(
                testResult.getTotal(), testResult.getFailures(), testResult.getErrors(), testResult.getSolved(), testResult.getStatusCode(),
                testMessage.getUserId(), testMessage.getProblemId(),
                testMessage.getLanguageId(), testMessage.getSolutionId()
        );
        kafka.send(resultTopic, messageId, trm).whenComplete((result, exception) -> {
            if (exception != null) {
                metrics.recordFailure(testMessage.getLanguageId(), "result_send");
            }
        });
    }

    @KafkaListener(topics = "test-topic", groupId = "test-group")
    protected void listen(
            @Payload TestMessage testMessage,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String messageId
    ) throws Exception {
        log.info("Consumed record with key: {}", messageId);

        final var runnerStart = System.currentTimeMillis();
        final var language = testMessage.getLanguageId();

        try {
            log.debug("Parsed record with key: {}, value: {}", messageId, testMessage);

            final var tests = new String(minioClient.getObject(GetObjectArgs.builder()
                    .bucket("problems")
                    .object(testMessage.getProblemTestPath())
                    .build()).readAllBytes());
            final var solution = new String(minioClient.getObject(GetObjectArgs.builder()
                    .bucket("solutions")
                    .object(testMessage.getSolutionFilePath())
                    .build()).readAllBytes());

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
            log.info("Delegating to {}", languageTester.getClass().getName());

            final var tester = new Tester(dockerClient, testRunnerProperties.getContainer().getTtk(), languageTester, testerConfig);
            tester.test(tests, solution)
                    .thenAccept(Context.current().wrapConsumer(testResult -> {
                        try {
                            handleResult(testResult, messageId, testMessage);
                            metrics.recordExecution(
                                    language,
                                    testResult.getSolved(),
                                    testResult.getStatusCode(),
                                    System.currentTimeMillis() - runnerStart
                            );
                        } catch (RuntimeException exception) {
                            log.error("Failed to handle test result for id {}", messageId, exception);
                        }
                    }))
                    .exceptionally(exception -> {
                        metrics.recordFailure(language, "docker_execution");
                        log.error("Failed to execute tests for id {}", messageId, exception);
                        return null;
                    });
        } catch (Exception exception) {
            metrics.recordFailure(language, "listen");
            throw exception;
        }
    }
}
