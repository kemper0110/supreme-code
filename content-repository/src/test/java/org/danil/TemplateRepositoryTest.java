package org.danil;

import org.danil.model.Language;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TemplateRepositoryTest {
    @Autowired
    private TemplateRepository repository;
    @Test
    void readLanguagesTemplates() {
        final var cppTemplate = repository.getBySlugAndLanguage("TwoSum", Language.Cpp);
        assertNotNull(cppTemplate);
        final var javaTemplate = repository.getBySlugAndLanguage("TwoSum", Language.Java);
        assertNotNull(javaTemplate);
        final var javascriptTemplate = repository.getBySlugAndLanguage("TwoSum", Language.Javascript);
        assertNotNull(javascriptTemplate);
    }
    @SpringBootApplication
    static class TestConfiguration {
    }
}