package org.supremecode.web.controller

import io.minio.GetObjectArgs
import io.minio.MinioClient
import lombok.RequiredArgsConstructor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.supremecode.web.repository.JdbcProblemRepositoryImpl
import org.supremecode.web.repository.ProblemRepository
import org.supremecode.web.service.MinioPathService
import org.supremecode.web.service.TestRunnerSenderService
import org.supremecode.web.views.ProblemLanguageSolveView
import org.supremecode.web.views.ProblemSolveView
import org.supremecode.web.views.ProblemView
import org.supremecode.web.views.SolutionResultSolveView
import org.supremecode.web.views.SolutionSolveView
import reactor.core.publisher.Mono

@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/problem")
class ProblemController(
    private val jdbcProblemRepository: JdbcProblemRepositoryImpl,
    private val testRunnerSenderService: TestRunnerSenderService,
    private val problemRepository: ProblemRepository,
    private val minioClient: MinioClient,
    private val minioPathService: MinioPathService,
) {
    var logger: Logger = LoggerFactory.getLogger(ProblemController::class.java)

    @GetMapping
    fun index(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) difficulty: String?,
        @RequestParam(required = false) languages: MutableList<String?>?,
        @RequestParam(required = false) tags: MutableList<String?>?
    ): Mono<List<ProblemView>> {
        return Mono.just(jdbcProblemRepository.findFiltered(name, difficulty, languages, tags))
    }

    data class TestRequest(val code: String, val language: String)

    @PostMapping("/{problemId}")
    fun submit(
        @PathVariable problemId: Long,
        @RequestBody testRequest: TestRequest,
        @AuthenticationPrincipal(expression = "id") userId: Long
    ): Mono<Long> {
        logger.debug("submitted solution for {} with {}", problemId, testRequest)
        return testRunnerSenderService.send(userId, testRequest.code, problemId, testRequest.language)
    }

    @GetMapping("/{problemId}")
    @Transactional(readOnly = true)
    fun view(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal(expression = "id") userId: Long
    ): Mono<ProblemSolveView?> {
        val problem = this.problemRepository.findById(problemId).get()
        return Mono.just(
            ProblemSolveView(
                problem.id!!,
                problem.name,
                problem.description,
                problem.difficulty,
                problem.languages.associate { language ->
                    val problemPaths = minioPathService.buildProblemPaths(problemId, language.languageId)
                    language.languageId to ProblemLanguageSolveView(
                        String(
                            minioClient.getObject(
                                GetObjectArgs.builder()
                                    .bucket("problems")
                                    .`object`(problemPaths.solutionTemplate)
                                    .build()
                            ).readBytes()
                        ),
                        language.solutions.map { solution ->
                            val solutionPath = minioPathService.buildSolutionPaths(userId, problemId, language.languageId, solution.id!!)
                            SolutionSolveView(
                                solution.id!!, solution.createdAt,
                                String(
                                  minioClient.getObject(
                                      GetObjectArgs.builder()
                                          .bucket("solutions")
                                          .`object`(solutionPath.file)
                                          .build()
                                  ).readAllBytes()
                                ),
                                if (solution.solutionResult != null)
                                    SolutionResultSolveView(
                                        solution.solutionResult!!.createdAt,
                                        solution.solutionResult!!.exitCode,
                                        solution.solutionResult!!.total,
                                        solution.solutionResult!!.failures,
                                        solution.solutionResult!!.errors,
                                        solution.solutionResult!!.solved,
                                    )
                                else null
                            )
                        }
                    )
                },
                problem.problemTags.map { t -> t.tag.id!! }.toMutableList(),
            )
        )
    }
}
