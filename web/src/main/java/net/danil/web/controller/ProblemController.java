package net.danil.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.danil.web.dto.DetailProblemDto;
import net.danil.web.model.Language;
import net.danil.web.model.Problem;
import net.danil.web.model.ProblemLanguage;
import net.danil.web.repository.ProblemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/problem")
public class ProblemController {
    Logger logger = LoggerFactory.getLogger(ProblemController.class);
    private final ProblemRepository problemRepository;

    final private KafkaTemplate<String, String> kafka;
    private SubscribableChannel resultsChannel;

    {
        this.resultsChannel = MessageChannels
                .publishSubscribe("TestResultsChannel")
                .getObject();
    }

    @GetMapping
    List<Problem> index() {
        return problemRepository.findAll();
    }

    record TestRequest(String code, Language language) {
    }

    record TestMessage(String code, String test, Language language) {
    }

    @PostMapping("/{id}")
    Mono<Object> submit(@PathVariable Long id, @RequestBody TestRequest testRequest) {
        final var problem = problemRepository.findById(id).get();
        logger.debug("submitted solution {}", testRequest);
        final var mapper = new ObjectMapper();
        final var testMessage = new TestMessage(testRequest.code(), problem.getLanguages().get(0).getTest(), testRequest.language());
        return Mono.create(sink -> {
            try {
                kafka.send("test-topic", mapper.writeValueAsString(testMessage));
            } catch (JsonProcessingException e) {
                sink.error(new RuntimeException(e));
                return;
            }
            MessageHandler handler = message -> sink.success((String) message.getPayload());
            sink.onDispose(() -> resultsChannel.unsubscribe(handler));
            resultsChannel.subscribe(handler);
        });
    }

    @KafkaListener(topics = "test-result-topic")
    public void listen(String in) {
        System.out.println("received result: " + in);
        resultsChannel.send(new GenericMessage<>(in));
    }

    @GetMapping("/{id}")
    DetailProblemDto view(@PathVariable Long id) {
        return problemRepository.findDetailedById(id).map(DetailProblemDto::fromProblem).get();
    }
}
