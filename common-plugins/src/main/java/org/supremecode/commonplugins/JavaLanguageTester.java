package org.supremecode.commonplugins;

import org.supremecode.pluginsdk.JunitParser;
import org.supremecode.pluginsdk.LanguageTester;
import org.supremecode.pluginsdk.result.TestExecutionResult;

public class JavaLanguageTester implements LanguageTester {
    @Override
    public String languageId() {
        return "java";
    }

    @Override
    public String imageName() {
        return "sc-java-test";
    }

    @Override
    public String testsPath() {
        return "/src/test/java/JunitTest.java";
    }

    @Override
    public String solutionPath() {
        return "/src/main/java/org/example/Solution.java";
    }

    @Override
    public String reportPath() {
        return "/usr/app/target/surefire-reports/TEST-JunitTest.xml";
    }

    @Override
    public TestExecutionResult verdict(String report, int exitCode) {
        try {
            final var parser = new JunitParser();
            final var testsuite = parser.parseTestsuite(report);
            final var total = Integer.parseInt(testsuite.getTests());
            final var failures = Integer.parseInt(testsuite.getFailures());
            final var errors = testsuite.getErrors() == null ? 0 : Integer.parseInt(testsuite.getErrors());
            final var solved = failures == 0 && errors == 0 && exitCode == 0;
            return new TestExecutionResult(total, failures, errors, solved);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
