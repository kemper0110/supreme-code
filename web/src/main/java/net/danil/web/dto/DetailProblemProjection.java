package net.danil.web.dto;

import net.danil.web.model.Language;
import net.danil.web.model.Problem;

import java.util.List;

/**
 * Projection for {@link net.danil.web.model.Problem}
 */
public interface DetailProblemProjection {
    Long getId();

    String getName();

    String getDescription();

    Problem.Difficulty getDifficulty();

    Boolean getActive();

    List<ProblemLanguageInfo> getLanguages();

    /**
     * Projection for {@link net.danil.web.model.ProblemLanguage}
     */
    interface ProblemLanguageInfo {
        Long getId();

        Language getLanguage();

        String getTemplate();

//        String getTest();
    }
}