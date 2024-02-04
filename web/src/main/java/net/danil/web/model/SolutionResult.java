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

@Table("solution_result")
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
    @NotNull
    @Column(length = 131_072) // 128kb
    private String result;
}
