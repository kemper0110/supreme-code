package net.danil.web.problem.dto;

public record TestResult(Long solutionId, int tests, int failures, int errors,
                         int statusCode, float time, String xml,
                         String logs) {
}
