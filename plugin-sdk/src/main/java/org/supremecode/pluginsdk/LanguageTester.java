package org.supremecode.pluginsdk;

import org.supremecode.pluginsdk.result.TestExecutionResult;

public interface LanguageTester {
    String languageId();
    String imageName();
    String testsPath();
    String solutionPath();
    String reportPath();
    TestExecutionResult verdict(String report, int exitCode);
}
