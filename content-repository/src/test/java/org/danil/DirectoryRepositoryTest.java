package org.danil;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DirectoryRepositoryTest {
    @Autowired
    private DirectoryRepository repository;
    @Test
    void getBySlugAndLanguage() {
        final var javaPath = repository.getBySlugAndLanguage("TwoSum", org.danil.model.Language.Java);
        assertTrue(javaPath.toFile().exists());
    }


    @SpringBootApplication
    static class TestConfiguration {
    }
}