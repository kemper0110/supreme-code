package org.supremecode.web.views

import org.supremecode.web.domain.Problem
import java.util.Date

data class ProblemSlightView(
    val id: Long,
    val name: String,
    val difficulty: String,
    val languages: MutableList<String>,
    val tags: MutableList<Long>
)

data class ProblemView(
    val id: Long,
    val name: String,
    val description: String,
    val difficulty: String,
    val languages: MutableList<String>,
    val tags: MutableList<Long>
)

fun mapProblemToView(p: Problem): ProblemView {
    return ProblemView(
        p.id!!, p.name, p.description, p.difficulty,
        p.languages.map { l -> l.languageId }.toMutableList(),
        p.problemTags.map { t -> t.tag.id!! }.toMutableList(),
    )
}

data class ProblemSolveView(
    val id: Long,
    val name: String,
    val description: String,
    val difficulty: String,
    val languages: Map<String, ProblemLanguageSolveView>,
    val tags: MutableList<Long>,
)

data class ProblemLanguageSolveView(
    val solutionTemplate: String,
    val solutions: List<SolutionSolveView>,
)

data class SolutionSolveView(
    val id: Long,
    val createdAt: Date,
    val code: String,
    val solutionResult: SolutionResultSolveView?,
)

data class SolutionResultSolveView(
    val createdAt: Date,
    var exitCode: Int,
    var total: Int,
    var failures: Int,
    var errors: Int,
    var solved: Boolean,
)
