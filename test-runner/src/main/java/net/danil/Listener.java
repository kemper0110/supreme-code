package net.danil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.danil.dto.TestMessage;
import net.danil.dto.TestResult;
import org.danil.DirectoryRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class Listener {
    static String resultTopic = "test-result-topic";
    final private KafkaTemplate<String, TestResult> kafka;
    final private JavascriptTester javascriptTester;
    final private CppTester cppTester;
    final private JavaTester javaTester;
    final private DirectoryRepository directoryRepository;

    @KafkaListener(topics = "test-topic", groupId = "test-group")
    protected void listen(@Payload TestMessage testMessage, @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String messageId) {
        log.info("Consumed record with key: {}", messageId);

        final var runnerStart = System.currentTimeMillis();

        log.debug("Parsed record with key: {}, value: {}", messageId, testMessage);

        final var path = directoryRepository.getBySlugAndLanguage(testMessage.testSlug(), testMessage.language());
        log.debug("Path: {}", path);

        java.util.function.Consumer<TestResult.TestResultBuilder> onResult = resultBuilder -> {
            final var runnerEnd = System.currentTimeMillis();
            log.info("Tests for id {} finished after {}ms", messageId, runnerEnd - runnerStart);
            final var result = resultBuilder
                    .solutionId(testMessage.solutionId())
                    .build();
            kafka.send(resultTopic, messageId, result);
        };

        switch (testMessage.language()) {
            case Javascript -> javascriptTester.test(path, testMessage.code(), onResult);
            case Cpp -> cppTester.test(path, testMessage.code(), onResult);
            case Java -> javaTester.test(path, testMessage.code(), onResult);
            default -> onResult.accept(TestResult.builder().logs("the language is not supported"));
        }
    }
}
