package org.supremecode.shared

data class TestResultMessage(
    val total: Int,
    val failures: Int,
    val errors: Int,
    val solved: Boolean,
    val statusCode: Int,
    val userId: Long, val problemId: Long, val languageId: String,
    val solutionId: Long,
)