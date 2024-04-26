package net.danil.web.problem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.danil.web.user.model.User;
import org.danil.model.Language;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Solution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String problemSlug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    @OneToOne(mappedBy = "solution", cascade = CascadeType.ALL)
    private SolutionResult solutionResult;
}
