package net.danil.web.repository;

import lombok.RequiredArgsConstructor;
import org.danil.model.Language;
import org.danil.model.Problem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class JdbcProblemRepository {
    private final JdbcTemplate jdbcTemplate;

    public Object findFiltered(String name, String difficulty, List<String> languages, List<String> tags) {
        final var hasDifficulty = difficulty != null && !difficulty.isEmpty();
        final var hasLanguages = languages != null && !languages.isEmpty();
        final var hasTags = tags != null && !tags.isEmpty();
        final var hasName = name != null && !name.isEmpty();
        final var hasAny = hasDifficulty || hasLanguages || hasTags || hasName;

        final var sql = "select problem_slug as id, name, description, difficulty, languages, tags " +
                        "from supreme_code.problem " +
                        (
                                hasAny ? "where " + String.join(" and ",
                                        Stream.of(
                                                hasName ? "name like CONCAT('%',?,'%')" : null,
                                                hasDifficulty ? "difficulty::varchar = ?" : null,
                                                hasLanguages ? "languages::varchar[] && ?" : null,
                                                hasTags ? "tags::varchar[] && ?" : null
                                        ).filter(Objects::nonNull).toList())
                                        : "");

        System.out.println(sql);
        final var params = new ArrayList<>(4);
        if (hasName)
            params.add(name);
        if (hasDifficulty)
            params.add(difficulty);
        if (hasLanguages)
            params.add(languages.toArray(new String[0]));
        if (hasTags)
            params.add(tags.toArray(new String[0]));

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Problem(rs.getString("id"), rs.getString("name"), rs.getString("description"),
                        Problem.Difficulty.valueOf(rs.getString("difficulty")),
                        Arrays.stream((String[]) rs.getArray("languages").getArray()).map(Language::valueOf).toList(),
                        List.of((String[]) rs.getArray("tags").getArray())
                ), params.toArray());
    }
}
