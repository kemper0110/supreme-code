package org.danil;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(JacksonAutoConfiguration.class)
public class ContentRepositoryConfiguration {
    @Bean
    YAMLMapper yamlMapper() {
        return new YAMLMapper();
    }
}
