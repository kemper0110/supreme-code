package net.danil.web.model;

import jakarta.persistence.*;
import lombok.*;

@Table
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    // TODO: run OWASP Java HTML Sanitizer
    private String description;

    enum Difficulty {
        Easy, Normal, Hard
    }

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;
}
