package org.supremecode.pluginsdk.result;

public record TestCase(
        String id,
        String name,
        String status,
        String message,
        String errorType,
        String errorMessage,
        String errorDetails,
        long executionTimeMs,
        String stdout,
        String stderr
) {
}
