package net.danil;

import com.github.dockerjava.api.DockerClient;
import lombok.extern.slf4j.Slf4j;
import net.danil.dto.TestResult;
import net.danil.generated.junit.Testsuites;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JavascriptTester extends Tester {
    public JavascriptTester(DockerClient dockerClient) {
        super(dockerClient);
    }

    @Override
    protected String solutionFilename() {
        return "/solution.js";
    }

    @Override
    protected String containerName() {
        return "danil1digits0nly/sc-js-test:1.0";
    }

    @Override
    protected String reportPath() {
        return "/usr/app/junit.xml";
    }

    @Override
    protected TestResult.TestResultBuilder normalizeReport(Object parsedReport) {
        final var testsuites = (Testsuites) parsedReport;
        return TestResult.builder()
                .tests(Integer.parseInt(testsuites.getTests()))
                .failures(Integer.parseInt(testsuites.getFailures()))
                .errors(Integer.parseInt(testsuites.getErrors()))
                .time(Double.parseDouble(testsuites.getTime()));
    }
}
