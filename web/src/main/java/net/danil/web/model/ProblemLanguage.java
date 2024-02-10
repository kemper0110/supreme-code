package net.danil.web.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProblemLanguage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Problem problem;

    @OneToMany(mappedBy = "problemLanguage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Solution> solutions;

    @Enumerated(EnumType.STRING)
    private Language language;

    private String template;
    private String test;
}
