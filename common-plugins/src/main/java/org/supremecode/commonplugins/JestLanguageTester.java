package org.supremecode.commonplugins;

import org.supremecode.pluginsdk.JunitParser;
import org.supremecode.pluginsdk.LanguageTester;
import org.supremecode.pluginsdk.result.TestExecutionResult;

public class JestLanguageTester implements LanguageTester {
    @Override
    public TestExecutionResult verdict(String report, int exitCode) {
        try {
            final var parser = new JunitParser();
            final var testsuites = parser.parseTestsuites(report);
            final var total = Integer.parseInt(testsuites.getTests());
            final var failures = Integer.parseInt(testsuites.getFailures());
            final var errors = testsuites.getErrors() == null ? 0 : Integer.parseInt(testsuites.getErrors());
            final var solved = failures == 0 && errors == 0 && exitCode == 0;
            return new TestExecutionResult(total, failures, errors, solved);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
