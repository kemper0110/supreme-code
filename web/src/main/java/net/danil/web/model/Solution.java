package net.danil.web.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Table(name = "solution")
@Entity
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
    public static class SolutionId implements Serializable {
        private Long problemLanguageId;
        private Long userId;
    }

    @Id
    private Long userId;
    @Id
    private Long problemLanguageId;

    @Column(nullable = false, length = 65_536)
    private String code;
}
