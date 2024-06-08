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
    @Value("${supreme-code.content-repository.root}")
    private String root;
    @Value("${supreme-code.content-repository.problem.path}")
    private String problemRoot;

    public Path getBySlugAndLanguage(String slug, Language language) {
        return Paths.get(root, problemRoot, slug, language.toString());
    }
}
