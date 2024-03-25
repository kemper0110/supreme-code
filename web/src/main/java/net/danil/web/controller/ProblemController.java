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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/problem")
public class ProblemController {
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
    Mono<Map<String, ?>> index() {
        final var problemSlugs = contentRepository.get().getProblems();
        return Mono.just(Map.of(
                "problems", problemSlugs.stream().map(slug -> new ProblemEntry(slug, problemRepository.getBySlug(slug))).toList()
        ));
    }

    public record TestRequest(String code, Language language) {
    }

    @PostMapping("/{slug}")
    Mono<Object> submit(@PathVariable String slug, @RequestBody TestRequest testRequest, @AuthenticationPrincipal(expression = "id") Long userId) {
        logger.debug("submitted solution for {} with {}", slug, testRequest);
        return testRunnerSenderService.send(userId, testRequest.code(), slug, testRequest.language())
                .flatMap(taskId -> testRunnerChannelService.subscribe(taskId.toString()))
                .map(message -> {
                    final var solution = (Solution) message.getPayload();
                    final var solutionResult = solution.getSolutionResult();
                    return new SolutionView(solution.getId(), solution.getCode(), solution.getProblemSlug(), solution.getLanguage(), solutionResult == null ? null : new SolutionResultView(solutionResult.getId(), solutionResult.getTests(), solutionResult.getFailures(), solutionResult.getErrors(), solutionResult.getStatusCode(), solutionResult.getTime(), solutionResult.getLogs(), solutionResult.getJunitXml(), solutionResult.getSolved()));
                });
    }


    record LanguageTemplate(Language language, String template) {

    }

    record SolutionResultView(Long id, Integer tests, Integer failures, Integer errors, Integer statusCode, Float time,
                              String logs, String junitXml, Boolean solved) {
    }

    record SolutionView(Long id, String code, String problemSlug, Language language,
                        SolutionResultView solutionResult) {
    }

    record ProblemView(String id, String name, String description, Problem.Difficulty difficulty,
                       List<LanguageTemplate> languages, List<SolutionView> solutions) {

    }

    @GetMapping("/{slug}")
    Mono<ProblemView> view(@PathVariable String slug, @AuthenticationPrincipal(expression = "id") Long userId) {
        final var problem = problemRepository.getBySlug(slug);
        final var solutions = solutionRepository.findByProblemSlugAndUserIdOrderByIdDesc(slug, userId).stream();
        return Mono.just(new ProblemView(problem.getId(), problem.getName(), problem.getDescription(), problem.getDifficulty(), problem.getLanguages().stream().map(lang -> new LanguageTemplate(lang, templateRepository.getBySlugAndLanguage(slug, lang))).toList(), solutions.map(s -> {
            final var solutionResult = s.getSolutionResult();
            return new SolutionView(s.getId(), s.getCode(), s.getProblemSlug(), s.getLanguage(), solutionResult == null ? null : new SolutionResultView(solutionResult.getId(), solutionResult.getTests(), solutionResult.getFailures(), solutionResult.getErrors(), solutionResult.getStatusCode(), solutionResult.getTime(), solutionResult.getLogs(), solutionResult.getJunitXml(), solutionResult.getSolved()));
        }).toList()));
    }
}
