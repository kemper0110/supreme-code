package net.danil.web.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Table(name = "problem_language")
@Entity
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
    public static class ProblemLanguageId implements Serializable {
        private Long problemId;
        private Long userId;
    }

    @Id
    private Long problemId;
    @Id
    private Long userId;

    @Column(nullable = false)
    @Enumerated
    private Language language;

    @Column(length = 2048, nullable = false)
    private String template;
    @Column(length = 65_536, nullable = false)
    private String test;
}
