package net.danil;

import lombok.Builder;

@Builder
public record TestResult(int tests, int failures, int errors, double time, String xml, String logs) {
}
