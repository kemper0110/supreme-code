package net.danil.web.dto;

import net.danil.web.model.Language;
import net.danil.web.model.ProblemLanguage;

public record BasicProblemLanguageDto(Long id, Language language, String template) {
    public static BasicProblemLanguageDto fromProblemLanguage(ProblemLanguage problemLanguage) {
        return new BasicProblemLanguageDto(problemLanguage.getId(), problemLanguage.getLanguage(), problemLanguage.getTemplate());
    }
}
