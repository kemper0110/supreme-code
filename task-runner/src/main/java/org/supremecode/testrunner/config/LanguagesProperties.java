package org.supremecode.testrunner.config;

import lombok.Data;
import org.supremecode.testrunner.Language;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "supreme-code.task-runner")
public class LanguagesProperties {
    private Map<Language, LanguageConfig> languages = new HashMap<>();
}