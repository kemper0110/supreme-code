package org.supremecode.testrunner.config;

import lombok.Data;
import org.supremecode.testrunner.Language;

@Data
public class LanguageConfig {
    private Language language;
    private String image;
    private String cmd;
    private String codeFilename;
}