package org.supremecode.pluginsdk.result;

public record Summary(
        int total,
        int passed,
        int failures,
        int errors,
        int skipped
) {
}
