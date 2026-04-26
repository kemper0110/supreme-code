package org.supremecode.web.repository

import jakarta.annotation.PostConstruct
import org.springframework.jdbc.core.ResultSetExtractor
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import org.supremecode.web.views.ProblemView
import org.supremecode.web.views.TagView

@Repository
open class JdbcProblemRepositoryImpl(
    private val jdbc: NamedParameterJdbcTemplate
){
    @PostConstruct
    fun init() {
        println("JdbcProblemRepository instance = $this")
        println("jdbc = $jdbc")
        println("INIT jdbc field identity = ${System.identityHashCode(jdbc)}")
    }

    fun findFiltered(
        name: String?,
        difficulty: String?,
        languages: MutableList<String?>?,
        tags: MutableList<String?>?
    ): List<ProblemView> {
        println("JdbcProblemRepository instance = $this")
        println("jdbc = $jdbc")
        println("METHOD jdbc field identity = ${System.identityHashCode(jdbc)}")

        val sql = StringBuilder(
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

        val params = MapSqlParameterSource()

        if (name != null && !name.isBlank()) {
            sql.append(" and p.name ILIKE :name")
            params.addValue("name", "%" + name + "%")
        }

        if (difficulty != null && !difficulty.isBlank()) {
            sql.append(" and p.difficulty = :difficulty")
            params.addValue("difficulty", difficulty)
        }

        if (languages != null && !languages.isEmpty()) {
            sql.append(sql.append(" "))
            sql.append(
                """
                        and exists (
                            select 1 from supreme_code.problem_language ple
                            where ple.problem_id = p.id
                              and ple.language_id in (:languages)
                        )
                    
                    """.trimIndent()
            )
            params.addValue("languages", languages)
        }

        if (tags != null && !tags.isEmpty()) {
            sql.append(sql.append(" "))
            sql.append(
                """
                        and exists (
                            select 1 from supreme_code.problem_tags pt
                            join tag t on t.id = pt.tag_id
                            where pt.problem_id = p.id
                              and t.name in (:tags)
                        )
                    
                    """.trimIndent()
            )
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

                // language
                val lang = rs.getString("language_id")
                if (lang != null && !problem.languages.contains(lang)) {
                    problem.languages.add(lang)
                }

                // tag
                if (!rs.wasNull()) {
                    val tagId = rs.getLong("tag_id")
                    if (!problem.tags.contains(tagId))
                        problem.tags.add(tagId)
                }
            }

            map.values.toList()
        })!!
    }
}
