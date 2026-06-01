package org.supremecode.web.controller

import io.minio.GetObjectArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.supremecode.web.domain.Problem
import org.supremecode.web.domain.ProblemLanguage
import org.supremecode.web.domain.ProblemTag
import org.supremecode.web.domain.User
import org.supremecode.web.repository.ProblemLanguageRepository
import org.supremecode.web.repository.ProblemRepository
import org.supremecode.web.repository.TagRepository
import org.supremecode.web.repository.UserRepository
import org.supremecode.web.service.MinioPathService
import org.supremecode.web.views.ProblemView
import org.supremecode.web.views.mapProblemToView
import reactor.core.publisher.Mono
import java.io.ByteArrayInputStream

@RestController
@RequestMapping("/api/my-problem")
class MyProblemControllerImpl(
    private val problemRepository: ProblemRepository,
    private val tagRepository: TagRepository,
    private val minioClient: MinioClient,
    private val userRepository: UserRepository,
    private val minioPathService: MinioPathService,
    private val problemLanguageRepository: ProblemLanguageRepository,
) {
    @GetMapping
    @PreAuthorize("hasAuthority('my-problem:read')")
    @Transactional(readOnly = true)
    fun getMyProblems(auth: Authentication): Mono<List<ProblemView>> {
        val authUser = auth.details as User
        val problems = if (canAccessAnyUserProblem(auth)) {
            problemRepository.findAll()
        } else {
            problemRepository.findAllByAuthorId(authUser.id!!)
        }
        return Mono.just(problems.map { p -> mapProblemToView(p) })
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('my-problem:delete')")
    fun deleteMyProblem(@PathVariable id: Long, auth: Authentication): Mono<Void> {
        val problem = getAccessibleProblem(id, auth)
        this.problemRepository.delete(problem)
        return Mono.empty()
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('my-problem:read')")
    @Transactional(readOnly = true)
    fun getMyProblem(@PathVariable id: Long, auth: Authentication): Mono<MyProblemView> {
        val problem = getAccessibleProblem(id, auth)

        val problemSave = MyProblemView(
            problem.id!!,
            problem.name,
            problem.description,
            problem.difficulty,
            problem.languages.associate { l ->
                val pathPrefix = "/problems/${problem.id}/languages/${l.languageId}/"
                val readFile = { path: String ->
                    String(
                        minioClient.getObject(
                            GetObjectArgs.builder()
                                .bucket("problems")
                                .`object`(pathPrefix + path)
                                .build()
                        ).readBytes()
                    )
                }
                l.languageId to MyProblemLanguageView(
                    readFile("test.txt"),
                    readFile("solution.txt"),
                    readFile("solution-template.txt"),
                )
            },
            problem.problemTags.map { t -> t.tag.id!! }
        )

        return Mono.just(problemSave)
    }

    data class MyProblemView(
        val id: Long?,
        val name: String,
        val description: String,
        val difficulty: String,
        val languages: Map<String, MyProblemLanguageView>,
        val tags: List<Long>,
    )

    data class MyProblemLanguageView(
        val test: String,
        val solution: String,
        val solutionTemplate: String,
    )

    @PostMapping
    @PreAuthorize("hasAuthority('my-problem:create') or hasAuthority('my-problem:update')")
    fun saveMyProblem(@RequestBody problem: MyProblemView, auth: Authentication): Mono<Void> {
        val authUser = auth.details as User
        val p = if (problem.id == null) {
            requireAuthority(auth, "my-problem:create")
            Problem().also {
                it.author = userRepository.getReferenceById(authUser.id!!)
            }
        } else {
            requireAuthority(auth, "my-problem:update")
            getAccessibleProblem(problem.id, auth)
        }
        p.name = problem.name
        p.description = problem.description
        p.difficulty = problem.difficulty

        val existingLanguages = p.languages.associateBy { it.languageId }
        problem.languages.forEach { (langId) ->
            val existingLang = existingLanguages[langId]
            if (existingLang == null) {
                p.languages.add(ProblemLanguage(langId, p))
            }
        }
        // Удаляем языки, которых нет в запросе
        p.languages.removeAll { it.languageId !in problem.languages.keys }

        p.problemTags.clear()
        p.problemTags.addAll(
            problem.tags.map { ProblemTag(p, tagRepository.getReferenceById(it)) }.toMutableList()
        )
        val savedProblem = this.problemRepository.save(p)

        for (lang in problem.languages) {
            val paths = minioPathService.buildProblemPaths(savedProblem.id!!, lang.key)
            val putObject = { path: String, content: String ->
                val bytes = content.toByteArray()
                minioClient.putObject(
                    PutObjectArgs.builder()
                        .bucket("problems")
                        .`object`(path)
                        .contentType("text/plain")
                        .stream(ByteArrayInputStream(bytes), bytes.size.toLong(), -1)
                        .build()
                )
            }
            putObject(paths.test, lang.value.test)
            putObject(paths.solution, lang.value.solution)
            putObject(paths.solutionTemplate, lang.value.solutionTemplate)
        }
        return Mono.empty()
    }

    private fun getAccessibleProblem(id: Long, auth: Authentication): Problem {
        val authUser = auth.details as User
        val problem = problemRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Problem with id $id not found") }

        if (!canAccessAnyUserProblem(auth) && problem.author.id != authUser.id) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Problem with id $id not found")
        }

        return problem
    }

    private fun canAccessAnyUserProblem(auth: Authentication): Boolean {
        return auth.authorities.any { it.authority == "my-problem:any-user" }
    }

    private fun requireAuthority(auth: Authentication, authority: String) {
        if (auth.authorities.none { it.authority == authority }) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN)
        }
    }
}
