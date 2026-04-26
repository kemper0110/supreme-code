package org.supremecode.testrunner.dto

data class TestResult(
    val total: Int,
    val failures: Int,
    val errors: Int,
    val solved: Boolean,
    val statusCode: Int,
    val report: String,
    val logs: String,
)

