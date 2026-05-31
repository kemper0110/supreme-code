package org.supremecode.testrunner.dto

import org.supremecode.pluginsdk.result.TestCase

data class TestResult(
    val total: Int,
    val failures: Int,
    val errors: Int,
    val solved: Boolean,
    val statusCode: Int,
    val report: String,
    val logs: String,
    val testCases: List<TestCase>,
)

