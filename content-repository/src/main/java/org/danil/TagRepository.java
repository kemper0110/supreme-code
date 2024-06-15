package org.danil;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.RequiredArgsConstructor;
import org.danil.model.Tag;
import org.danil.model.Tags;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TagRepository {
    @Value("${supreme-code.content-repository.root}")
    private String root;
    @Value("${supreme-code.content-repository.path}")
    private String contentRoot;
    private final YAMLMapper objectMapper;
    public List<Tag> get() {
        try {
            final var path = Paths.get(root, contentRoot, "tags.yaml");
            final var content = Files.readString(path, StandardCharsets.UTF_8);
            return objectMapper.readValue(content, Tags.class).getTags();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
