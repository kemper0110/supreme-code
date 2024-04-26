package net.danil.web.problem.model;

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

    @Column(nullable = false)
    private Integer tests;

    @Column(nullable = false)
    private Integer failures;

    @Column(nullable = false)
    private Integer errors;

    @Column(nullable = false)
    private Integer statusCode;

    @Column(nullable = false)
    private Float time;

    @Column(nullable = false)
    private String logs;

    private String junitXml;

    @Column(nullable = false)
    private Boolean solved;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private Solution solution;
}
