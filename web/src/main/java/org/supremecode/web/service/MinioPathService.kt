package org.supremecode.web.service

class MinioPathService {
    data class ProblemPaths(
        val folder: String,
        val test: String,
        val solution: String,
        val solutionTemplate: String,
    );
    fun buildProblemPaths(problemId: Long, languageId: String): ProblemPaths {
        val folder = "/problems/${problemId}/languages/${languageId}/"
        return ProblemPaths(
            folder,
            folder + "test.txt",
            folder + "solution.txt",
            folder + "solution-template.txt",
        )
    }

    data class SolutionPaths(
        val folder: String,
        val file: String,
    )

    fun buildSolutionPaths(userId: Long, problemId: Long, language: String, solutionId: Long): SolutionPaths {
        val folder = "/users/$userId/problems/$problemId/languages/$language/solutions/${solutionId}/"
        val file = folder + "solution.txt"
        return SolutionPaths(
            folder, file
        )
    }
}