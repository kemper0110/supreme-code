package org.supremecode.commonplugins;

import org.supremecode.pluginsdk.junit.Error;
import org.supremecode.pluginsdk.junit.Failure;
import org.supremecode.pluginsdk.junit.Testcase;
import org.supremecode.pluginsdk.junit.Testsuite;
import org.supremecode.pluginsdk.junit.Testsuites;
import org.supremecode.pluginsdk.result.Summary;
import org.supremecode.pluginsdk.result.TestCase;
import org.supremecode.pluginsdk.result.TestExecutionResult;
import org.supremecode.pluginsdk.result.TestStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

final class JunitResultMapper {
    private static final int MESSAGE_LIMIT = 500;

    private JunitResultMapper() {
    }

    static TestExecutionResult fromTestsuite(Testsuite testsuite, int exitCode) {
        return fromTestsuites(List.of(testsuite), exitCode);
    }

    static TestExecutionResult fromTestsuites(Testsuites testsuites, int exitCode) {
        return fromTestsuites(testsuites.getTestsuite(), exitCode);
    }

    private static TestExecutionResult fromTestsuites(List<Testsuite> testsuites, int exitCode) {
        final var testCases = testsuites.stream()
                .flatMap(testsuite -> mapTestCases(testsuite).stream())
                .toList();

        final var total = testsuites.stream()
                .mapToInt(testsuite -> parseInt(testsuite.getTests(), testsuite.getTestcase().size()))
                .sum();
        final var failures = testsuites.stream()
                .mapToInt(testsuite -> parseInt(testsuite.getFailures(), countByStatus(testsuite, TestStatus.FAILED)))
                .sum();
        final var errors = testsuites.stream()
                .mapToInt(testsuite -> parseInt(testsuite.getErrors(), countByStatus(testsuite, TestStatus.ERROR)))
                .sum();
        final var skipped = testsuites.stream()
                .mapToInt(testsuite -> parseInt(testsuite.getSkipped(), countByStatus(testsuite, TestStatus.SKIPPED)))
                .sum();
        final var passed = Math.max(0, total - failures - errors - skipped);
        final var solved = failures == 0 && errors == 0 && exitCode == 0;

        return new TestExecutionResult(
                new Summary(total, passed, failures, errors, skipped),
                testCases,
                solved
        );
    }

    private static List<TestCase> mapTestCases(Testsuite testsuite) {
        final var testCases = new ArrayList<TestCase>();
        for (Testcase testcase : testsuite.getTestcase()) {
            testCases.add(new TestCase(
                    testcase.getName(),
                    suiteName(testsuite, testcase),
                    status(testcase),
                    message(testcase),
                    durationMs(testcase.getTime())
            ));
        }
        return testCases;
    }

    private static TestStatus status(Testcase testcase) {
        if (!testcase.getError().isEmpty()) {
            return TestStatus.ERROR;
        }
        if (!testcase.getFailure().isEmpty()) {
            return TestStatus.FAILED;
        }
        if (notBlank(testcase.getSkipped())) {
            return TestStatus.SKIPPED;
        }
        return TestStatus.PASSED;
    }

    private static String message(Testcase testcase) {
        if (!testcase.getError().isEmpty()) {
            final Error error = testcase.getError().getFirst();
            return shortMessage(error.getMessage(), error.getContent());
        }
        if (!testcase.getFailure().isEmpty()) {
            final Failure failure = testcase.getFailure().getFirst();
            return shortMessage(failure.getMessage(), failure.getContent());
        }
        if (notBlank(testcase.getSkipped())) {
            return shortMessage(testcase.getSkipped());
        }
        return null;
    }

    private static String suiteName(Testsuite testsuite, Testcase testcase) {
        if (meaningfulSuiteName(testcase.getClassname(), testcase.getName())) {
            return testcase.getClassname();
        }
        if (meaningfulSuiteName(testsuite.getName(), testcase.getName())) {
            return testsuite.getName();
        }
        return null;
    }

    private static boolean meaningfulSuiteName(String suiteName, String testName) {
        if (!notBlank(suiteName) || suiteName.equals(testName)) {
            return false;
        }
        final var normalized = suiteName.trim();
        return !normalized.equalsIgnoreCase("undefined") && !normalized.equals("(empty)");
    }

    private static Long durationMs(String seconds) {
        if (!notBlank(seconds)) {
            return null;
        }
        try {
            return new BigDecimal(seconds.trim())
                    .multiply(BigDecimal.valueOf(1000))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValue();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int parseInt(String value, int defaultValue) {
        if (!notBlank(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static int countByStatus(List<TestCase> testCases, TestStatus status) {
        return (int) testCases.stream()
                .filter(testCase -> testCase.status() == status)
                .count();
    }

    private static int countByStatus(Testsuite testsuite, TestStatus status) {
        return (int) testsuite.getTestcase().stream()
                .filter(testcase -> status(testcase) == status)
                .count();
    }

    private static String shortMessage(String... candidates) {
        for (String candidate : candidates) {
            if (notBlank(candidate)) {
                final var trimmed = candidate.trim();
                if (trimmed.length() <= MESSAGE_LIMIT) {
                    return trimmed;
                }
                return trimmed.substring(0, MESSAGE_LIMIT);
            }
        }
        return null;
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
