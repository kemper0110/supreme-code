package org.danil;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.danil.model.Problem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Repository
@RequiredArgsConstructor
public class ProblemRepository {
    @Value("${supreme-code.content-repository.problem.path}")
    private String problemRoot;
    private final YamlObjectMapper objectMapper;
    public Problem getBySlug(String slug) {
        // TODO: sanitize slug
        final var path = problemRoot + "/" + slug + "/manifest.yaml";
        try {
            final var problem = IOUtils.resourceToString(path, StandardCharsets.UTF_8);
            return objectMapper.readValue(problem, Problem.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
