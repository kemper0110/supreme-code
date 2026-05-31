package org.supremecode.shared

data class TestCaseResult(
    val name: String? = null,
    val suiteName: String? = null,
    val status: String? = null,
    val message: String? = null,
    val durationMs: Long? = null,
)
