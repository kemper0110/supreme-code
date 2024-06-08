package org.danil;

import org.apache.commons.io.IOUtils;
import org.danil.model.Language;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Repository
public class TemplateRepository {
    @Value("${supreme-code.content-repository.root}")
    private String root;
    @Value("${supreme-code.content-repository.problem.path}")
    private String problemRoot;

    public String getBySlugAndLanguage(String slug, Language language) {
        // TODO: sanitize slug
        try {
            final var path = Paths.get(root, problemRoot, slug, language.toString(), switch (language) {
                case Cpp -> "/solution.hpp";
                case Java -> "/src/main/java/org/example/Solution.java";
                case Javascript -> "/solution.js";
            });
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
