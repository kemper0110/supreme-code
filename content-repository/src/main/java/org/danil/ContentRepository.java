package org.danil;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.danil.model.Content;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Repository
@RequiredArgsConstructor
public class ContentRepository {
    @Value("${supreme-code.content-repository.path}")
    private String contentRoot;
    private final YamlObjectMapper objectMapper;
    Content get() {
        final var path = contentRoot + "/manifest.yaml";
        try {
            final var content = IOUtils.resourceToString(path, StandardCharsets.UTF_8);
            return objectMapper.readValue(content, Content.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}