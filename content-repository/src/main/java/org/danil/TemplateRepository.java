package org.danil;

import org.apache.commons.io.IOUtils;
import org.danil.model.Language;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Repository
public class TemplateRepository {
    @Value("${supreme-code.content-repository.problem.path}")
    private String problemRoot;

    String getBySlug(String slug, Language language) {
        // TODO: sanitize slug
        final var problemPath = problemRoot + "/" + slug + "/" + language.toString();
        final var templatePath = switch (language) {
            case Cpp -> problemPath + "/main.cpp";
            case Java -> problemPath + "/src/main/java/org/danil/Main.java";
            case Javascript -> problemPath + "/main.js";
        };
        try {
            return IOUtils.resourceToString(templatePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
