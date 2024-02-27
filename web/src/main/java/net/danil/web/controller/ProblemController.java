package net.danil.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.danil.web.service.TestRunnerChannelService;
import org.danil.ContentRepository;
import org.danil.ProblemRepository;
import org.danil.TemplateRepository;
import org.danil.model.Language;
import org.danil.model.Problem;
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

    final private KafkaTemplate<String, String> kafka;

    final private TestRunnerChannelService testRunnerChannelService;

    final private TemplateRepository templateRepository;
    final private ProblemRepository problemRepository;
    final private ContentRepository contentRepository;

    record ProblemEntry(String slug, Problem problem) {

    }

    @GetMapping
    List<ProblemEntry> index() {
        final var problemSlugs = contentRepository.get().getProblems();
        return problemSlugs.stream().map(slug -> new ProblemEntry(slug, problemRepository.getBySlug(slug))).toList();
    }

    record TestRequest(String code, Language language) {
    }

    record TestMessage(String code, String testSlug, Language language) {
    }

    public record TestResult(int tests, int failures, int errors, int statusCode, double time, String xml,
                             String logs) {
    }

    @PostMapping("/{slug}")
    Mono<Object> submit(@PathVariable String slug, @RequestBody TestRequest testRequest) {
        logger.debug("submitted solution for {} with {}", slug, testRequest);
        final var mapper = new ObjectMapper();
        final var testMessage = new TestMessage(testRequest.code(), slug, testRequest.language());
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


    record LanguageTemplate(
            Language language,
            String template
    ) {

    }

    record ProblemView(
            String id,
            String name,
            String description,
            Problem.Difficulty difficulty,
            List<LanguageTemplate> languages
    ) {

    }

    @GetMapping("/{slug}")
    ProblemView view(@PathVariable String slug) {
        final var problem = problemRepository.getBySlug(slug);
        return new ProblemView(
                problem.getId(),
                problem.getName(),
                problem.getDescription(),
                problem.getDifficulty(),
                problem.getLanguages().stream().map(lang ->
                                new LanguageTemplate(lang,
                                        templateRepository.getBySlugAndLanguage(slug, lang)))
                        .toList()
        );
    }
    //        return problemRepository.findDetailedById(id).get();
//        return templateRepository.getBySlugAndLanguage("TwoSum", org.danil.model.Language.Java);
}
