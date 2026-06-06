package org.supremecode.web.controller

import io.minio.GetObjectArgs
import io.minio.MinioClient
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.supremecode.web.domain.Solution
import org.supremecode.web.domain.User
import org.supremecode.web.repository.JdbcProblemRepositoryImpl
import org.supremecode.web.repository.ProblemRepository
import org.supremecode.web.repository.SolutionRepository
import org.supremecode.web.service.MinioPathService
import org.supremecode.web.service.TestRunnerSenderService
import org.supremecode.web.views.ProblemLanguageSolveView
import org.supremecode.web.views.ProblemSolveView
import org.supremecode.web.views.ProblemView
import org.supremecode.web.views.SolutionCodeView
import org.supremecode.web.views.SolutionResultSolveView
import org.supremecode.web.views.SolutionSolveView
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/problem")
class ProblemController(
    private val jdbcProblemRepository: JdbcProblemRepositoryImpl,
    private val testRunnerSenderService: TestRunnerSenderService,
    private val problemRepository: ProblemRepository,
    private val solutionRepository: SolutionRepository,
    private val minioClient: MinioClient,
    private val minioPathService: MinioPathService,
) {
    private val logger = LoggerFactory.getLogger(ProblemController::class.java)

    @GetMapping
    @PreAuthorize("hasAuthority('problem:list')")
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
    @PreAuthorize("hasAuthority('solution:submit')")
    fun submit(
        @PathVariable problemId: Long,
        @RequestBody testRequest: TestRequest,
        auth: Authentication
    ): Mono<Long> {
        val authUser = auth.details as User
        val userId = authUser.id!!
        logger.debug("submitted solution for {} with {}", problemId, testRequest)
        return testRunnerSenderService.send(userId, testRequest.code, problemId, testRequest.language)
    }

    @GetMapping("/{problemId}")
    @PreAuthorize("hasAuthority('problem:view')")
    @Transactional(readOnly = true)
    fun view(
        @PathVariable problemId: Long,
        auth: Authentication
    ): Mono<ProblemSolveView> {
        val authUser = auth.details as User
        val userId = authUser.id!!
        val problem = this.problemRepository.findById(problemId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        return Mono.just(
            ProblemSolveView(
                problem.id!!,
                problem.name,
                problem.description,
                problem.difficulty,
                problem.languages.associate { language ->
                    val problemPaths = minioPathService.buildProblemPaths(problemId, language.languageId)
                    language.languageId to ProblemLanguageSolveView(
                        readMinioText(PROBLEMS_BUCKET, problemPaths.solutionTemplate),
                        language.solutions
                            .filter { solution -> solution.author.id == userId }
                            .sortedWith(solutionOrder)
                            .map(::mapSolutionToView)
                    )
                },
                problem.problemTags.map { t -> t.tag.id!! }.toMutableList(),
            )
        )
    }

    @GetMapping("/{problemId}/solution/{solutionId}/code")
    @PreAuthorize("hasAuthority('solution:view')")
    @Transactional(readOnly = true)
    fun solutionCode(
        @PathVariable problemId: Long,
        @PathVariable solutionId: Long,
        auth: Authentication
    ): Mono<SolutionCodeView> {
        val authUser = auth.details as User
        val userId = authUser.id!!
        val solution = solutionRepository.findById(solutionId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }
        val problemLanguage = solution.problemLanguage
        if (solution.author.id != userId || problemLanguage.problem.id != problemId) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }

        val solutionPath = minioPathService.buildSolutionPaths(
            userId,
            problemId,
            problemLanguage.languageId,
            solution.id!!
        )
        return Mono.just(
            SolutionCodeView(
                problemLanguage.languageId,
                readMinioText(SOLUTIONS_BUCKET, solutionPath.file)
            )
        )
    }

    private fun mapSolutionToView(solution: Solution): SolutionSolveView {
        return SolutionSolveView(
            solution.id!!,
            solution.createdAt,
            solution.solutionResult?.let { result ->
                SolutionResultSolveView(
                    result.createdAt,
                    result.exitCode,
                    result.total,
                    result.failures,
                    result.errors,
                    result.solved,
                    result.testCases,
                )
            }
        )
    }

    private fun readMinioText(bucket: String, path: String): String {
        return String(
            minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(path)
                    .build()
            ).readBytes()
        )
    }

    companion object {
        private const val PROBLEMS_BUCKET = "problems"
        private const val SOLUTIONS_BUCKET = "solutions"

        private val solutionOrder = compareByDescending<Solution> { it.createdAt }
            .thenByDescending { it.id ?: Long.MIN_VALUE }
    }
}
