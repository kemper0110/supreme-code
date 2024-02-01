package net.danil;

import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import io.confluent.kafka.serializers.KafkaJsonDeserializer;
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

public class TaskRunnerApplication {
    static String taskTopic = "task-topic";
    static String resultTopic = "result-topic";

    record Task(String code, String language) {

    }

    public static Consumer<String, Task> makeConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaJsonDeserializer.class);
        Consumer<String, Task> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(taskTopic));
        return consumer;
    }

    public static Producer<String, String> makeProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new KafkaProducer<>(props);
    }

    public static void main(String[] args) {
        System.out.println("Cpp runner initialized");

        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://localhost:2375")
                .build();

        Supplier<DockerHttpClient> httpClientProvider = () -> new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();


        final var cppRunner = new CppRunner(config, httpClientProvider.get());
        final var javaRunner = new JavaRunner(config, httpClientProvider.get());
        final var javascriptRunner = new JavascriptRunner(config, httpClientProvider.get());


        try (Consumer<String, Task> consumer = makeConsumer();
             Producer<String, String> producer = makeProducer()
        ) {
            System.out.println("Kafka initialized, started consuming");
            int i = 0;
            while (true) {
                ConsumerRecords<String, Task> records = consumer.poll(Duration.ofMillis(1000));
                System.out.println("Consume timeout " + i++);
                records.forEach(record -> {
                    final var task = record.value();
                    System.out.printf("Consumed record with key %s and value %s%n", record.key(), record.value());

                    final var runnerStart = System.currentTimeMillis();

                    final var result = switch (task.language) {
                        case "c++" -> cppRunner.run(task.code);
                        case "java" -> javaRunner.run(task.code);
                        case "javascript" -> javascriptRunner.run(task.code);
                        default -> "Unknown language";
                    };

                    final var runnerEnd = System.currentTimeMillis();
                    System.out.println("Runner finished after " + (runnerEnd - runnerStart) + " ms");

                    producer.send(new ProducerRecord<>(resultTopic, record.key(), result));
                });
            }
        }
    }
}