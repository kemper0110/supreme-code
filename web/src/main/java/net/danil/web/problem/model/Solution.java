package net.danil.web.problem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.danil.web.user.model.User;
import org.danil.model.Language;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.Type;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;


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
    @ColumnTransformer(write="?::supreme_code.language_enum")
    private Language language;


    @OneToOne(mappedBy = "solution", cascade = CascadeType.ALL)
    private SolutionResult solutionResult;
}
