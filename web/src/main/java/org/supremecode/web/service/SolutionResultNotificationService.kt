package org.supremecode.web.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import org.supremecode.shared.TestResultMessage
import reactor.core.publisher.Flux

data class SolutionResultNotification(
    val problemId: Long,
    val solutionId: Long,
    val languageId: String,
    val solved: Boolean,
    val statusCode: Int,
    val total: Int,
    val failures: Int,
    val errors: Int,
)

@Service
class SolutionResultNotificationService(
    private val redisTemplate: StringRedisTemplate,
    private val listenerContainer: RedisMessageListenerContainer,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(SolutionResultNotificationService::class.java)

    fun publish(testResult: TestResultMessage) {
        try {
            val notification = SolutionResultNotification(
                problemId = testResult.problemId,
                solutionId = testResult.solutionId,
                languageId = testResult.languageId,
                solved = testResult.solved,
                statusCode = testResult.statusCode,
                total = testResult.total,
                failures = testResult.failures,
                errors = testResult.errors,
            )
            val channel = channel(testResult.userId)
            redisTemplate.convertAndSend(channel, objectMapper.writeValueAsString(notification))
        } catch (e: Exception) {
            logger.warn("failed to publish solution result notification", e)
        }
    }

    fun listen(userId: Long): Flux<SolutionResultNotification> {
        return Flux.create { sink ->
            val topic = ChannelTopic(channel(userId))
            val listener = MessageListener { message: Message, _ ->
                try {
                    val payload = message.body.toString(Charsets.UTF_8)
                    sink.next(objectMapper.readValue(payload, SolutionResultNotification::class.java))
                } catch (e: Exception) {
                    logger.warn("failed to read solution result notification", e)
                }
            }

            listenerContainer.addMessageListener(listener, topic)
            sink.onDispose {
                listenerContainer.removeMessageListener(listener, topic)
            }
        }
    }

    private fun channel(userId: Long) = "solution-result:$userId"
}
