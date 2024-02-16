package net.danil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.function.Supplier;

public class TestRunnerApplication {

    static String testTopic = "test-topic";
    static String resultTopic = "test-result-topic";

    record Test(String code, String test, String language) {

    }


    public static Consumer<String, String> makeConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        Consumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(testTopic));
        return consumer;
    }

    public static Producer<String, String> makeProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new KafkaProducer<>(props);
    }

    public static void main(String... args) {
        System.out.println("Test runner initialized");

        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
//                .withDockerHost("unix:///var/run/docker.sock")
                .withDockerHost("tcp://localhost:2375")
                .build();

        Supplier<DockerHttpClient> httpClientProvider = () -> new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        final var javascriptTester = new JavascriptTester(config, httpClientProvider.get());

        try (Consumer<String, String> consumer = makeConsumer();
             Producer<String, String> producer = makeProducer()
        ) {
            System.out.println("Kafka initialized, started consuming");
            int i = 0;
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                System.out.println("Consume timeout " + i++);
                records.forEach(record -> {
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
                            producer.send(new ProducerRecord<>(resultTopic, record.key(), mapper.writeValueAsString(result)));
                        } catch (JsonProcessingException e) {
                            System.out.println(e.getMessage());
                        }
                    };

                    switch (test.language) {
//                        case "c++" -> cppRunner.run(task.code);
//                        case "java" -> javaRunner.run(task.code);
                        case "javascript" -> javascriptTester.test(test.test, test.code, onResult);
                        default -> onResult.accept("aboba exception: unknown language");
                    };
                });
            }
        }
    }
}
