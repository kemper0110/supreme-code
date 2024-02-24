package net.danil.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.danil.web.dto.DetailProblemProjection;
import net.danil.web.model.Language;
import net.danil.web.model.Problem;
import net.danil.web.repository.ProblemLanguageRepository;
import net.danil.web.repository.ProblemRepository;
import net.danil.web.service.TestRunnerChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/problem")
public class ProblemController {
    Logger logger = LoggerFactory.getLogger(ProblemController.class);
    private final ProblemRepository problemRepository;
    private final ProblemLanguageRepository problemLanguageRepository;

    final private KafkaTemplate<String, String> kafka;

    final private TestRunnerChannelService testRunnerChannelService;

    @GetMapping
    List<Problem> index() {
        return problemRepository.findAll();
    }

    record TestRequest(String code, Language language) {
    }

    record TestMessage(String code, String test, Language language) {
    }
    public record TestResult(int tests, int failures, int errors, double time, String xml, String logs) {
    }

    @PostMapping("/{id}")
    Mono<Object> submit(@PathVariable Long id, @RequestBody TestRequest testRequest) {
        logger.debug("submitted solution {}", testRequest);
        final var mapper = new ObjectMapper();
        final var problemLanguage = problemLanguageRepository.findByProblemIdAndLanguage(id, testRequest.language()).get();
        final var testMessage = new TestMessage(testRequest.code(), problemLanguage.getTest(), testRequest.language());
        return Mono.create(sink -> {
            final var taskId = UUID.randomUUID().toString();
            try {
                kafka.send("test-topic", taskId, mapper.writeValueAsString(testMessage));
            } catch (JsonProcessingException e) {
                sink.error(new RuntimeException(e));
                return;
            }
            sink.onDispose(() -> testRunnerChannelService.unsubscribe(taskId));
            testRunnerChannelService.subscribe(taskId, message -> {
                try {
                    final TestResult result = mapper.readValue((String) message.getPayload(), TestResult.class);
                    sink.success(result);
                } catch (JsonProcessingException e) {
                    sink.error(new RuntimeException(e));
                }
            });
        });
    }

    @GetMapping("/{id}")
    DetailProblemProjection view(@PathVariable Long id) {
        return problemRepository.findDetailedById(id).get();
    }
}
