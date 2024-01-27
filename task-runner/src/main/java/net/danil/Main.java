package net.danil;

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

public class Main {
    static String taskTopic = "task-topic";
    static String resultTopic = "result-topic";

    public static Consumer<String, String> makeConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        Consumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(taskTopic));
        return consumer;
    }

    public static Producer<String, String> makeProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return new KafkaProducer<>(props);
    }

    public static void main(String[] args) {
        final var cppRunner = new CppRunner();
        System.out.println("Cpp runner initialized");

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
                    final var result = cppRunner.apply(record.value());
                    final var runnerEnd = System.currentTimeMillis();
                    System.out.println("Runner finished after " + (runnerEnd - runnerStart) + " ms");

                    producer.send(new ProducerRecord<>(resultTopic, record.key(), result));
                });
            }
        }
    }
}