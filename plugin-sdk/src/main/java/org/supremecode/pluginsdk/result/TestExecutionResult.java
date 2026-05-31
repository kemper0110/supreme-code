package org.supremecode.pluginsdk.result;

import java.util.List;

public record TestExecutionResult(
        Summary summary,
        List<TestCase> testCases,
        boolean solved
) {
}

