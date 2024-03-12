package net.danil.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.danil.web.service.TaskRunnerChannelService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Mono;

import java.util.UUID;

//@Controller
@RequiredArgsConstructor
public class PlaygroundController {
    final private KafkaTemplate<String, String> kafka;
    final private TaskRunnerChannelService taskRunnerChannelService;

    @GetMapping("/")
    String index() {
        return "index";
    }

    record TaskRequest(String code, String language) {
    }

    @ResponseBody
    @PostMapping("/api/")
    Mono<String> run(@RequestBody TaskRequest taskRequest) {
        return Mono.create(sink -> {
            final var taskId = UUID.randomUUID().toString();
            try {
                final var mapper = new ObjectMapper();
                kafka.send("task-topic", taskId, mapper.writeValueAsString(taskRequest));
            } catch (JsonProcessingException e) {
                sink.error(new RuntimeException(e));
                return;
            }
            sink.onDispose(() -> taskRunnerChannelService.unsubscribe(taskId));
            taskRunnerChannelService.subscribe(taskId, message -> sink.success((String) message.getPayload()));
        });
    }
}
