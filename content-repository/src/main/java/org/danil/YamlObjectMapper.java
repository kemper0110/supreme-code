package org.danil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.stereotype.Component;

@Component
public class YamlObjectMapper extends ObjectMapper {
    public YamlObjectMapper() {
        super(new YAMLFactory());
    }
}
