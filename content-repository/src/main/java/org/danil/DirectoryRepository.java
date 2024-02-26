package org.danil;

import lombok.RequiredArgsConstructor;
import org.danil.model.Language;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Repository
@RequiredArgsConstructor
public class DirectoryRepository {
    @Value("${supreme-code.content-repository.problem.path}")
    private String problemRoot;

    public Path getBySlugAndLanguage(String slug, Language language) {
        try {
            final var problemPath = problemRoot + "/" + slug + "/" + language.toString();
            final var classloader = this.getClass().getClassLoader();
            final var resource = classloader.getResource(problemPath);
            assert resource != null;
            return Paths.get(resource.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
