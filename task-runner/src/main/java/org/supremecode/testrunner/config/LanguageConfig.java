package org.supremecode.testrunner.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.supremecode.testrunner.Language;

@Data
@AllArgsConstructor
public class LanguageConfig {
    private Language language;
    private String image;
    private String cmd;
    private String codeFilename;
}
