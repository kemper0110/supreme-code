package org.supremecode.web.service

import io.minio.MinioClient
import io.minio.PutObjectArgs
import lombok.RequiredArgsConstructor
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Service
import org.supremecode.shared.TestMessage
import org.supremecode.web.domain.Solution
import org.supremecode.web.repository.ProblemLanguageRepository
import org.supremecode.web.repository.SolutionRepository
import org.supremecode.web.repository.UserRepository
import reactor.core.publisher.Mono
import java.io.ByteArrayInputStream

@Service
@RequiredArgsConstructor
class TestRunnerSenderService(
    private val kafka: ReactiveKafkaProducerTemplate<String, TestMessage>,
    private val solutionRepository: SolutionRepository,
    private val userRepository: UserRepository,
    private val problemLanguageRepository: ProblemLanguageRepository,
    private val minioClient: MinioClient,
    private val minioPathService: MinioPathService
) {

    fun send(userId: Long, code: String, problemId: Long, language: String): Mono<Long> {
        val user = userRepository.getReferenceById(userId)
        val problemLanguage = problemLanguageRepository.findByProblemIdAndLanguageId(problemId, language)
        val solution = solutionRepository.save(Solution(user, problemLanguage))

        val bytes = code.toByteArray()
        val solutionPaths = minioPathService.buildSolutionPaths(userId, problemId, language, solution.id!!)
        val problemPaths = minioPathService.buildProblemPaths(problemId, language)
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket("solutions")
                .`object`(solutionPaths.file)
                .contentType("text/plain")
                .stream(ByteArrayInputStream(bytes), bytes.size.toLong(), -1)
                .build()
        )
        return kafka.send(
            TOPIC_NAME, solution.id.toString(), TestMessage(
                userId, problemId, language, solution.id!!,
                solutionPaths.folder, solutionPaths.file,
                problemPaths.test,
            )
        ).map({ _ -> solution.id })
    }

    companion object {
        const val TOPIC_NAME: String = "test-topic"
    }
}
