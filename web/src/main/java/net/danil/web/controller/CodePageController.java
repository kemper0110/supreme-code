package net.danil.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.integration.dsl.MessageChannels;

import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class CodePageController {
    final private KafkaTemplate<String, String> kafka;
    private SubscribableChannel resultsChannel;
    {
        this.resultsChannel = MessageChannels
                .publishSubscribe("ResultsChannel")
                .getObject();
    }
    @GetMapping("/")
    String index() {
        return "index";
    }

    record TaskRequest(String code, String language) {}
    @ResponseBody
    @PostMapping("/api/")
    Mono<String> run(@RequestBody TaskRequest taskRequest) {
        final var mapper = new ObjectMapper();
        return Mono.create(sink -> {
            try {
                kafka.send("task-topic", mapper.writeValueAsString(taskRequest));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            MessageHandler handler = message -> sink.success((String) message.getPayload());
            sink.onDispose(() -> resultsChannel.unsubscribe(handler));
            resultsChannel.subscribe(handler);
        });
    }

    @KafkaListener(topics = "result-topic")
    public void listen(String in) {
        System.out.println("received result: " + in);
        resultsChannel.send(new GenericMessage<>(in));
    }
}
