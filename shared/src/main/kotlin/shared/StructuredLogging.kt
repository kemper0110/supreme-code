package org.supremecode.shared

import org.slf4j.MDC

data class MdcField(val key: String, val value: Any?)

fun interface MdcContext : AutoCloseable {
    override fun close()
}

object StructuredLogging {
    @JvmStatic
    fun field(key: String, value: Any?): MdcField = MdcField(key, value)

    @JvmStatic
    fun openContext(vararg fields: MdcField): MdcContext {
        val previousValues = fields.map { it.key }.toSet().associateWith { MDC.get(it) }
        fields.forEach { (key, value) ->
            if (value == null) {
                MDC.remove(key)
            } else {
                MDC.put(key, value.toString())
            }
        }
        return MdcContext {
            previousValues.forEach { (key, value) ->
                if (value == null) {
                    MDC.remove(key)
                } else {
                    MDC.put(key, value)
                }
            }
        }
    }

    @JvmStatic
    fun openTestMessageContext(testMessage: TestMessage, kafkaMessageId: String?, vararg fields: MdcField): MdcContext {
        return openContext(
            field("user_id", testMessage.userId),
            field("problem_id", testMessage.problemId),
            field("solution_id", testMessage.solutionId),
            field("language_id", testMessage.languageId),
            field("kafka_message_id", kafkaMessageId),
            *fields,
        )
    }
}

fun mdcContext(vararg fields: Pair<String, Any?>): MdcContext {
    return StructuredLogging.openContext(
        *fields.map { (key, value) -> MdcField(key, value) }.toTypedArray()
    )
}
