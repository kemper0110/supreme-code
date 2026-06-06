package org.supremecode.web.repository

import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.supremecode.web.views.ProblemView

@Repository
open class JdbcProblemRepositoryImpl(
    private val jdbc: NamedParameterJdbcTemplate
) {
    fun findFiltered(
        name: String?,
        difficulty: String?,
        languages: MutableList<String?>?,
        tags: MutableList<String?>?
    ): List<ProblemView> {
        val sql = baseProblemQuery()
        val params = MapSqlParameterSource()

        if (!name.isNullOrBlank()) {
            sql.append(" and p.name ILIKE :name")
            params.addValue("name", "%" + name + "%")
        }

        if (!difficulty.isNullOrBlank()) {
            sql.append(" and p.difficulty = :difficulty")
            params.addValue("difficulty", difficulty)
        }

        if (!languages.isNullOrEmpty()) {
            sql.append(languageFilter())
            params.addValue("languages", languages)
        }

        if (!tags.isNullOrEmpty()) {
            sql.append(tagFilter())
            params.addValue("tags", tags)
        }

        return jdbc.query(sql.toString(), params, ResultSetExtractor { rs ->
            val map = LinkedHashMap<Long, ProblemView>()

            while (rs.next()) {
                val problemId = rs.getLong("p_id")

                val problem = map.computeIfAbsent(problemId) {
                    ProblemView(
                        id = problemId,
                        name = rs.getString("p_name"),
                        description = rs.getString("p_description"),
                        difficulty = rs.getString("p_difficulty"),
                        languages = mutableListOf(),
                        tags = mutableListOf()
                    )
                }

                val language = rs.getString("language_id")
                if (language != null && !problem.languages.contains(language)) {
                    problem.languages.add(language)
                }

                val tagId = rs.getLong("tag_id")
                if (!rs.wasNull() && !problem.tags.contains(tagId)) {
                    problem.tags.add(tagId)
                }
            }

            map.values.toList()
        })!!
    }

    private fun baseProblemQuery() = StringBuilder(
        """
            select p.id as p_id,
                   p.name as p_name,
                   p.description as p_description,
                   p.difficulty as p_difficulty,
                   pt.tag_id as tag_id,
                   pl.language_id as language_id
            from supreme_code.problem p
            left join supreme_code.problem_tags pt on pt.problem_id = p.id
            left join supreme_code.problem_language pl on pl.problem_id = p.id
            where 1=1
        """.trimIndent()
    )

    private fun languageFilter() =
        """
             and exists (
                select 1 from supreme_code.problem_language ple
                where ple.problem_id = p.id
                  and ple.language_id in (:languages)
            )
        """.trimIndent().prependIndent(" ")

    private fun tagFilter() =
        """
             and exists (
                select 1 from supreme_code.problem_tags pt
                join tag t on t.id = pt.tag_id
                where pt.problem_id = p.id
                  and t.name in (:tags)
            )
        """.trimIndent().prependIndent(" ")
}
