package org.supremecode.shared

data class TestMessage(
    val userId: Long, val problemId: Long, val languageId: String, val solutionId: Long,
    val solutionFolderPath: String, val solutionFilePath: String,
    val problemTestPath: String,
)