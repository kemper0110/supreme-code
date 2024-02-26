package org.danil;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.danil.model.Problem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProblemRepository {
    @Value("${supreme-code.content-repository.problem.path}")
    private String problemRoot;
    private final YAMLMapper objectMapper;
    private final ContentRepository contentRepository;
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
    public List<Problem> getAll() {
        final var problemSlugs = contentRepository.get().getProblems();
        return problemSlugs.stream().map(this::getBySlug).toList();
    }
}
