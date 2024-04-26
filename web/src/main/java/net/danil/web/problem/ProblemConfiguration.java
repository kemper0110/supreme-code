package net.danil.web.problem;

import net.danil.web.problem.dto.TestMessage;
import net.danil.web.problem.dto.TestResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ProblemConfiguration {

    @Bean
    public SenderOptions<String, TestMessage> senderOptions(KafkaProperties kafkaProperties) {
        return SenderOptions.create(kafkaProperties.buildProducerProperties(null));
    }

    @Bean
    public ReactiveKafkaProducerTemplate<String, TestMessage> reactiveKafkaProducerTemplate(
            SenderOptions<String, TestMessage> senderOptions
    ) {
        return new ReactiveKafkaProducerTemplate<>(senderOptions);
    }


    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "web-group");
        return props;
    }

    @Bean
    public ConsumerFactory<String, TestResult> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
                consumerConfigs(),
                new StringDeserializer(),
                new JsonDeserializer<>(TestResult.class, false));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TestResult> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TestResult> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
