package net.danil.web.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("problem")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial")
    private Long id;
    @NotNull
    private String name;
    // TODO: run OWASP Java HTML Sanitizer
    @NotNull
    private String description;

    enum Difficulty {
        Easy, Normal, Hard
    }
    @NotNull
    private Difficulty difficulty;
}
