package net.danil.web.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SolutionResult {
    @Id
    private Long id;

    // TODO: think about compile error, test error, test success
    private String result;
    private Boolean isBuildError;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private Solution solution;
}
