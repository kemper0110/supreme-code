package net.danil;

import lombok.Builder;

@Builder
public record TestResult(int tests, int failures, int errors, int statusCode, double time, String xml, String logs) {
}
