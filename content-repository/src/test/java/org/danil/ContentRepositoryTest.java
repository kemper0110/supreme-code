package org.danil;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ContentRepositoryTest {
    @Autowired
    private ContentRepository repository;
    @Test
    void readContentManifest() {
        final var content = repository.get();
        assertNotNull(content);
        assertNotNull(content.getProblems());
    }

    @SpringBootApplication
    static class TestConfiguration {
    }
}