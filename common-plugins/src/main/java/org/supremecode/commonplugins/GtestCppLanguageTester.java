package org.supremecode.commonplugins;

import org.supremecode.pluginsdk.JunitParser;
import org.supremecode.pluginsdk.LanguageTester;
import org.supremecode.pluginsdk.result.TestExecutionResult;

public class GtestCppLanguageTester implements LanguageTester {
    @Override
    public TestExecutionResult verdict(String report, int exitCode) {
        try {
            final var parser = new JunitParser();
            final var testsuite = parser.parseTestsuite(report);
            return JunitResultMapper.fromTestsuite(testsuite, exitCode);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
