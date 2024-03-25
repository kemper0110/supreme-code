package net.danil;

import com.github.dockerjava.api.DockerClient;
import lombok.extern.slf4j.Slf4j;
import net.danil.dto.TestResult;
import net.danil.generated.junit.Testsuite;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CppTester extends Tester {
    public CppTester(DockerClient dockerClient) {
        super(dockerClient);
    }

    @Override
    protected String solutionFilename() {
        return "/solution.hpp";
    }

    @Override
    protected String containerName() {
        return "danil1digits0nly/sc-cpp-test:1.0";
    }

    @Override
    protected String reportPath() {
        return "/usr/app/build/junit.xml";
    }

    @Override
    protected TestResult.TestResultBuilder normalizeReport(Object parsedReport) {
        var testsuite = (Testsuite) parsedReport;
        final var time = testsuite.getTestcase().stream().mapToDouble(testcase -> testcase.getTime() == null ? 0D : Double.parseDouble(testcase.getTime())).sum();
        return TestResult.builder()
                .tests(Integer.parseInt(testsuite.getTests()))
                .failures(Integer.parseInt(testsuite.getFailures()))
                .errors(testsuite.getErrors() == null ? 0 : Integer.parseInt(testsuite.getErrors()))
                .time(time);
    }
}
