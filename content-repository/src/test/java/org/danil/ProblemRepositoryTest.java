package org.danil;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProblemRepositoryTest {
    @Autowired
    private ProblemRepository repository;

    @Test
    void readProblemManifest() {
        final var problem = repository.getBySlug("TwoSum");
        assertNotNull(problem);
        assertNotNull(problem.getId());
        assertNotNull(problem.getName());
        assertNotNull(problem.getDifficulty());
        assertNotNull(problem.getDescription());
        assertNotNull(problem.getLanguages());
        assertNotEquals(0, problem.getLanguages().size());
    }

    @SpringBootApplication
    static class TestConfiguration {
    }
}