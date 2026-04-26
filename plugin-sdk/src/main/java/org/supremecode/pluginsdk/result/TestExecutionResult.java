package org.supremecode.pluginsdk.result;

public record TestExecutionResult(
        int total,
        int failures,
        int errors,
        boolean solved
) {
}

