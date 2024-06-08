package org.danil;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.danil.model.Problem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProblemRepository {
    @Value("${supreme-code.content-repository.root}")
    private String root;
    @Value("${supreme-code.content-repository.problem.path}")
    private String problemRoot;
    private final YAMLMapper objectMapper;
    public Problem getBySlug(String slug) {
        // TODO: sanitize slug
        try {
            final var path = Paths.get(root, problemRoot, slug, "manifest.yaml");
            final var problem = Files.readString(path, StandardCharsets.UTF_8);
            return objectMapper.readValue(problem, Problem.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
