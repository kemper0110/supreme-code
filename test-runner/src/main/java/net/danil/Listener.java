package net.danil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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

    record Test(String code, String test, String language) {

    }

    @KafkaListener(topics = "test-topic", groupId = "test-group")
    protected void listen(ConsumerRecord<String, String> record) {
        System.out.printf("Consumed record with key %s and value %s%n", record.key(), record.value());

        final var runnerStart = System.currentTimeMillis();

        final var mapper = new ObjectMapper();
        final Test test;
        try {
            test = mapper.readValue(record.value(), Test.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        System.out.println("received " + test);

        java.util.function.Consumer<Object> onResult = result -> {
            final var runnerEnd = System.currentTimeMillis();
            System.out.println("Runner finished after " + (runnerEnd - runnerStart) + " ms");
            try {
                kafka.send(resultTopic, record.key(), mapper.writeValueAsString(result));
            } catch (JsonProcessingException e) {
                System.out.println(e.getMessage());
            }
        };

        switch (test.language) {
            case "Javascript" -> javascriptTester.test(test.test, test.code, onResult);
            default -> onResult.accept("aboba exception: unknown language");
        }
    }
}
