package net.danil.web.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SolutionResult {
    @Id
    private Long id;
    private Integer tests;
    private Integer failures;
    private Integer errors;
    private Integer statusCode;
    private Float time;
    private String logs;
    private String junitXml;
    private Boolean solved;
    private Solution solution;
}
