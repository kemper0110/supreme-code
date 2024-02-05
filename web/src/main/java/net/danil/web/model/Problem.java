package net.danil.web.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "problem")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial")
    private Long id;
    @Column(nullable = false, length = 50)
    private String name;
    // TODO: run OWASP Java HTML Sanitizer
    @Column(nullable = false, length = 4096)
    private String description;

    enum Difficulty {
        Easy, Normal, Hard
    }

    @Column(nullable = false)
    private Difficulty difficulty;
}
