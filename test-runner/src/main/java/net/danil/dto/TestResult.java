package net.danil.dto;

import lombok.Builder;

@Builder
public record TestResult(
        Long solutionId,
        int tests,
        int failures,
        int errors,
        int statusCode,
        double time,
        String xml,
        String logs) {
}
