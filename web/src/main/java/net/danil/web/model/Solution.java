package net.danil.web.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.danil.model.Language;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Solution {
    @Id
    private Long id;
    private User user;
    private String code;
    private String problemSlug;
    private Language language;
    private SolutionResult solutionResult;
}
