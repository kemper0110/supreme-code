package net.danil.web.content;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.danil.ContentRepository;
import org.danil.ProblemRepository;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@RequiredArgsConstructor
@Service
@Slf4j
public class ContentService {
    private final ContentRepository contentRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ProblemRepository problemRepository;

    public synchronized void updateContent() {
        jdbcTemplate.update("truncate table supreme_code.problem");

        final var content = contentRepository.get();
        final var problemsSlugs = content.getProblems();

        jdbcTemplate.batchUpdate("insert into supreme_code.problem(problem_slug, name, description, difficulty, languages) " +
                                 "values (?, ?, ?, ? ::supreme_code.difficulty_enum, ? ::supreme_code.language_enum[])", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                final var problemSlug = problemsSlugs.get(i);
                final var problem = problemRepository.getBySlug(problemSlug);
                ps.setString(1, problemSlug);
                ps.setString(2, problem.getName());
                ps.setString(3, problem.getDescription());
                ps.setString(4, problem.getDifficulty().name());

                final var languages = problem.getLanguages().stream().map(Enum::name).toArray(String[]::new);
                ps.setObject(5, languages);
            }

            @Override
            public int getBatchSize() {
                return problemsSlugs.size();
            }
        });
    }

    @PostConstruct
    public void initialUpdate() {
        updateContent();
    }
}
