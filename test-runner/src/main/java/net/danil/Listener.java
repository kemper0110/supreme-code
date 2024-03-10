package net.danil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.danil.dto.TestMessage;
import net.danil.dto.TestResult;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.danil.DirectoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Listener {
    private final Logger logger = LoggerFactory.getLogger(Listener.class);

    static String resultTopic = "test-result-topic";
    final private KafkaTemplate<String, String> kafka;
    final private JavascriptTester javascriptTester;
    final private DirectoryRepository directoryRepository;

    @KafkaListener(topics = "test-topic", groupId = "test-group")
    protected void listen(ConsumerRecord<String, String> record) {
        logger.info("Consumed record with key: {}", record.key());

        final var runnerStart = System.currentTimeMillis();

        final var mapper = new ObjectMapper();
        final TestMessage testMessage;
        try {
            testMessage = mapper.readValue(record.value(), TestMessage.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        logger.debug("Parsed record with key: {}, value: {}", record.key(), testMessage);

        final var path = directoryRepository.getBySlugAndLanguage(testMessage.testSlug(), testMessage.language());
        logger.debug("Path: {}", path);

        java.util.function.Consumer<TestResult.TestResultBuilder> onResult = resultBuilder -> {
            final var runnerEnd = System.currentTimeMillis();
            logger.info("Tests for id {} finished after {}ms", record.key(), runnerEnd - runnerStart);
            final var result = resultBuilder
                    .solutionId(testMessage.solutionId())
                    .build();
            try {
                kafka.send(resultTopic, record.key(), mapper.writeValueAsString(result));
            } catch (JsonProcessingException e) {
                logger.error("listener error={}", e.getMessage());
            }
        };

        switch (testMessage.language()) {
            case Javascript -> javascriptTester.test(path, testMessage.code(), onResult);
            default -> onResult.accept(TestResult.builder().logs("the language is not supported"));
        }
    }
}
