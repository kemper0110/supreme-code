package net.danil.web;

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
                .publishSubscribe("GameListChannel")
                .getObject();
    }
    @GetMapping("/")
    String index() {
        return "index";
    }

    @ResponseBody
    @PostMapping("/")
    Mono<String> run(@RequestBody String code) {
        return Mono.create(sink -> {
            kafka.send("task-topic", code);
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
