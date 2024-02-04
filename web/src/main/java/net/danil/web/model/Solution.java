package net.danil.web.model;

import jakarta.persistence.IdClass;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("solution")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@IdClass(Solution.SolutionId.class)
public class Solution {
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SolutionId {
        private Long problemLanguageId;
        private Long userId;
    }
    @Id
    private Long userId;
    @Id
    private Long problemLanguageId;

    @NotNull
    private String code;
}
