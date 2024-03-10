package net.danil.web.controller;

import lombok.RequiredArgsConstructor;
import net.danil.web.model.Solution;
import net.danil.web.repository.SolutionRepository;
import net.danil.web.service.TestRunnerChannelService;
import net.danil.web.service.TestRunnerSenderService;
import org.danil.ContentRepository;
import org.danil.ProblemRepository;
import org.danil.TemplateRepository;
import org.danil.model.Language;
import org.danil.model.Problem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/problem")
public class ProblemController {
    // todo: remove mocked user id
    final Long MOCKEDUSERID = 1L;
    Logger logger = LoggerFactory.getLogger(ProblemController.class);

    final private TestRunnerChannelService testRunnerChannelService;

    final private TemplateRepository templateRepository;
    final private ProblemRepository problemRepository;
    final private ContentRepository contentRepository;
    final private TestRunnerSenderService testRunnerSenderService;
    private final SolutionRepository solutionRepository;

    record ProblemEntry(String slug, Problem problem) {

    }

    @GetMapping
    List<ProblemEntry> index() {
        final var problemSlugs = contentRepository.get().getProblems();
        return problemSlugs.stream().map(slug -> new ProblemEntry(slug, problemRepository.getBySlug(slug))).toList();
    }

    public record TestRequest(String code, Language language) {
    }

    public record TestResult(Long solutionId, int tests, int failures, int errors,
                             int statusCode, float time, String xml,
                             String logs) {
    }

    @PostMapping("/{slug}")
    Mono<Object> submit(@PathVariable String slug, @RequestBody TestRequest testRequest) {
        logger.debug("submitted solution for {} with {}", slug, testRequest);
        return Mono.create(sink -> {
            try {
                final var taskId = testRunnerSenderService.send(MOCKEDUSERID, testRequest.code(), slug, testRequest.language());
                sink.onDispose(() -> testRunnerChannelService.unsubscribe(taskId));

                testRunnerChannelService.subscribe(taskId, (message) -> {
                    if (message.getHeaders().containsKey("exception")) {
                        final var exception = message.getHeaders().get("exception");
                        sink.error(new RuntimeException((String) message.getPayload(), (Throwable) exception));
                    } else {
                        final TestResult result = (TestResult) message.getPayload();
                        sink.success(result);
                    }
                });
            } catch (Exception e) {
                sink.error(e);
            }
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
            List<LanguageTemplate> languages,
            TestResult result
    ) {

    }

    @GetMapping("/{slug}")
    ProblemView view(@PathVariable String slug) {
        final var problem = problemRepository.getBySlug(slug);
        final var solution = solutionRepository.findFirstByProblemSlugAndUserIdOrderByIdDesc(slug, MOCKEDUSERID);
        TestResult testResult = null;
        if(solution != null) {
            final var solutionResult = solution.getSolutionResult();
            testResult = new TestResult(solution.getId(), solutionResult.getTests(), solutionResult.getFailures(), solutionResult.getErrors(),
                    solutionResult.getStatusCode(), solutionResult.getTime(), solutionResult.getJunitXml(), solutionResult.getLogs());
        }
        return new ProblemView(
                problem.getId(),
                problem.getName(),
                problem.getDescription(),
                problem.getDifficulty(),
                problem.getLanguages().stream().map(lang ->
                                new LanguageTemplate(lang,
                                        templateRepository.getBySlugAndLanguage(slug, lang)))
                        .toList(),
                testResult
        );
    }
}
