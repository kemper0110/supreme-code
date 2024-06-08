package org.danil;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.danil.model.Content;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Repository
@RequiredArgsConstructor
public class ContentRepository {
    @Value("${supreme-code.content-repository.root}")
    private String root;
    @Value("${supreme-code.content-repository.path}")
    private String contentRoot;
    private final YAMLMapper objectMapper;
    public Content get() {
        try {
            final var path = Paths.get(root, contentRoot, "manifest.yaml");
            final var content = Files.readString(path, StandardCharsets.UTF_8);
            return objectMapper.readValue(content, Content.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
