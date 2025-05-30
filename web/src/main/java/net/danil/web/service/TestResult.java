package net.danil.web.service;

public record TestResult(Long solutionId, int tests, int failures, int errors,
                         int statusCode, float time, String xml,
                         String logs) {
}
