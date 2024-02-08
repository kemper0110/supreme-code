package net.danil.web.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SolutionResult {
    @Id
    @OneToOne
    private Solution solution;

    // TODO: think about compile error, test error, test success
    private String result;
    private Boolean isBuildError;
}
