package org.supremecode.testrunner;

import com.github.dockerjava.api.DockerClient;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.supremecode.shared.TestMessage;
import org.supremecode.shared.TestResultMessage;
import org.supremecode.testrunner.configuration.TestRunnerProperties;

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

    @KafkaListener(topics = "test-topic", groupId = "test-group")
    protected void listen(
            @Payload TestMessage testMessage,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String messageId
    ) throws Exception {
        log.info("Consumed record with key: {}", messageId);

        final var runnerStart = System.currentTimeMillis();

        log.debug("Parsed record with key: {}, value: {}", messageId, testMessage);

        final var tests = new String(minioClient.getObject(GetObjectArgs.builder()
                .bucket("problems")
                .object(testMessage.getProblemTestPath())
                .build()).readAllBytes());
        final var solution = new String(minioClient.getObject(GetObjectArgs.builder()
                .bucket("solutions")
                .object(testMessage.getSolutionFilePath())
                .build()).readAllBytes());

        final var languageTester = languagePluginService.getLanguageTester(testMessage.getLanguageId());
        if (languageTester == null) {
            throw new IllegalStateException("Unexpected value: " + testMessage.getLanguageId());
        }
        log.info("Delegating to {}", languageTester.getClass().getSimpleName());

        final var tester = new Tester(dockerClient, testRunnerProperties.getContainer().getTtk(), languageTester);
        tester.test(tests, solution).thenAccept(testResult -> {
            log.info("Tests for id {} finished after {}ms", messageId, System.currentTimeMillis() - runnerStart);

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
                throw new RuntimeException(e);
            }
            final var trm = new TestResultMessage(
                    testResult.getTotal(), testResult.getFailures(), testResult.getErrors(), testResult.getSolved(), testResult.getStatusCode(),
                    testMessage.getUserId(), testMessage.getProblemId(),
                    testMessage.getLanguageId(), testMessage.getSolutionId()
            );
            kafka.send(resultTopic, messageId, trm);
        });
    }
}
