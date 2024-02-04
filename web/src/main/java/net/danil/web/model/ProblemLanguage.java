package net.danil.web.model;

import jakarta.persistence.Column;
import jakarta.persistence.IdClass;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("problem_language")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@IdClass(ProblemLanguage.ProblemLanguageId.class)
public class ProblemLanguage {
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProblemLanguageId {
        private Long problemId;
        private Long userId;
    }

    @Id
    private Long problemId;
    @Id
    private Long userId;

    private Language language;

    @NotNull
    @Column(length = 2048)
    private String template;
    @NotNull
    @Column(length = 65_536)
    private String test;
}
