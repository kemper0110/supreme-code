package org.supremecode.web.controller

import io.minio.GetObjectArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import org.springframework.security.core.Authentication
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.supremecode.web.domain.Problem
import org.supremecode.web.domain.ProblemLanguage
import org.supremecode.web.domain.ProblemTag
import org.supremecode.web.repository.ProblemLanguageRepository
import org.supremecode.web.repository.ProblemRepository
import org.supremecode.web.repository.TagRepository
import org.supremecode.web.repository.UserRepository
import org.supremecode.web.service.MinioPathService
import org.supremecode.web.user.security.UserInfo
import org.supremecode.web.views.ProblemView
import org.supremecode.web.views.mapProblemToView
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
    @Transactional(readOnly = true)
    fun getMyProblems(): List<ProblemView> {
        return problemRepository.findAll()
            .map { p -> mapProblemToView(p) }
    }

    @DeleteMapping("/{id}")
    fun deleteMyProblem(@PathVariable id: Long) {
        this.problemRepository.deleteById(id)
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    fun getMyProblem(@PathVariable id: Long): MyProblemView {
        val problem = this.problemRepository.findById(id)
            .orElseThrow { RuntimeException("Problem with id $id not found") }

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

        return problemSave
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
    fun saveMyProblem(@RequestBody problem: MyProblemView, auth: Authentication) {
        val p = if (problem.id != null) {
            problemRepository.findById(problem.id)
                .orElseThrow { RuntimeException("Problem not found") }
        } else {
            Problem()
        }
        p.name = problem.name
        p.description = problem.description
        p.difficulty = problem.difficulty
        p.author = userRepository.getReferenceById((auth.principal as UserInfo).id)

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
        p.author = userRepository.getReferenceById((auth.principal as UserInfo).id);
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
    }
}