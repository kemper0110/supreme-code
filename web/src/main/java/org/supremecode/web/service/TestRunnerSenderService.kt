package org.supremecode.web.service

import io.minio.MinioClient
import io.minio.PutObjectArgs
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Service
import org.supremecode.shared.TestMessage
import org.supremecode.shared.mdcContext
import org.supremecode.web.domain.Solution
import org.supremecode.web.repository.ProblemLanguageRepository
import org.supremecode.web.repository.SolutionRepository
import org.supremecode.web.repository.UserRepository
import reactor.core.publisher.Mono
import java.io.ByteArrayInputStream

@Service
class TestRunnerSenderService(
    private val kafka: ReactiveKafkaProducerTemplate<String, TestMessage>,
    private val solutionRepository: SolutionRepository,
    private val userRepository: UserRepository,
    private val problemLanguageRepository: ProblemLanguageRepository,
    private val minioClient: MinioClient,
    private val minioPathService: MinioPathService,
    private val businessMetrics: BusinessMetrics
) {
    private val logger = LoggerFactory.getLogger(TestRunnerSenderService::class.java)

    fun send(userId: Long, code: String, problemId: Long, language: String): Mono<Long> {
        mdcContext(
            "user_id" to userId,
            "problem_id" to problemId,
            "language_id" to language,
        ).use {
            val solution = try {
                val user = userRepository.getReferenceById(userId)
                val problemLanguage = problemLanguageRepository.findByProblemIdAndLanguageId(problemId, language)
                val savedSolution = solutionRepository.save(Solution(user, problemLanguage))
                businessMetrics.recordSubmission(language)
                mdcContext("solution_id" to savedSolution.id).use { logger.info("solution submission received") }
                savedSolution
            } catch (exception: Exception) {
                logger.error("failed to create solution submission", exception)
                throw exception
            }

            val bytes = code.toByteArray()
            val solutionId = solution.id!!
            val kafkaMessageId = solutionId.toString()
            val solutionPaths = minioPathService.buildSolutionPaths(userId, problemId, language, solutionId)
            val problemPaths = minioPathService.buildProblemPaths(problemId, language)
            try {
                minioClient.putObject(
                    PutObjectArgs.builder()
                        .bucket("solutions")
                        .`object`(solutionPaths.file)
                        .contentType("text/plain")
                        .stream(ByteArrayInputStream(bytes), bytes.size.toLong(), -1)
                        .build()
                )
                mdcContext("solution_id" to solutionId).use { logger.debug("solution file uploaded") }
            } catch (exception: Exception) {
                mdcContext("solution_id" to solutionId).use { logger.error("failed to upload solution file", exception) }
                throw exception
            }
            return kafka.send(
                TOPIC_NAME, kafkaMessageId, TestMessage(
                    userId, problemId, language, solutionId,
                    solutionPaths.folder, solutionPaths.file,
                    problemPaths.test,
                )
            )
                .doOnSuccess {
                    mdcContext(
                        "user_id" to userId,
                        "problem_id" to problemId,
                        "solution_id" to solutionId,
                        "language_id" to language,
                        "kafka_message_id" to kafkaMessageId,
                    ).use {
                        logger.info("solution test message sent")
                    }
                }
                .doOnError { exception ->
                    mdcContext(
                        "user_id" to userId,
                        "problem_id" to problemId,
                        "solution_id" to solutionId,
                        "language_id" to language,
                        "kafka_message_id" to kafkaMessageId,
                    ).use {
                        logger.error("failed to send solution test message", exception)
                    }
                }
                .map { solutionId }
        }
    }

    companion object {
        const val TOPIC_NAME: String = "test-topic"
    }
}
