package net.danil.web.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "solution_result")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@IdClass(Solution.SolutionId.class)
public class SolutionResult {
    @Id
    private Long userId;
    @Id
    private Long problemLanguageId;

    // TODO: think about compile error, test error, test success
    @Column(length = 131_072, nullable = false) // 128kb
    private String result;
}
