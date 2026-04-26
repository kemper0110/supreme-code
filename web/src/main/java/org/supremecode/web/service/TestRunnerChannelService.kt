package org.supremecode.web.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.transaction.annotation.Transactional
import org.supremecode.shared.TestResultMessage
import org.supremecode.web.domain.SolutionResult
import org.supremecode.web.repository.SolutionRepository
import org.supremecode.web.repository.SolutionResultRepository

open class TestRunnerChannelService(
    val solutionRepository: SolutionRepository,
    val solutionResultRepository: SolutionResultRepository,
) {
    val logger: Logger = LoggerFactory.getLogger(TestRunnerChannelService::class.java)

    @KafkaListener(topics = [TOPIC_NAME])
    @Transactional
    open fun listen(
        @Payload testResult: TestResultMessage,
        @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) messageId: String?
    ) {
        if (messageId == null) {
            logger.error("received null message id")
            return
        }
        logger.info("received result forId({}): {}", messageId, testResult)
        val solution = solutionRepository.findById(testResult.solutionId).get()
        val solutionResult = SolutionResult(
            solution, testResult.statusCode, testResult.solved, testResult.total,
            testResult.failures, testResult.errors
        )
        solutionResultRepository.save(solutionResult)
    }

    companion object {
        const val TOPIC_NAME: String = "test-result-topic"
    }
}
