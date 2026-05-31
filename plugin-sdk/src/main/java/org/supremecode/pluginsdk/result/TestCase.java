package org.supremecode.pluginsdk.result;

public record TestCase(
        String name,
        String suiteName,
        TestStatus status,
        String message,
        Long durationMs
) {
}
