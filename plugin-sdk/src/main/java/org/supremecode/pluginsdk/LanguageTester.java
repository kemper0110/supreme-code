package org.supremecode.pluginsdk;

import org.supremecode.pluginsdk.result.TestExecutionResult;

public interface LanguageTester {
    TestExecutionResult verdict(String report, int exitCode);
}
