package net.danil;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import lombok.extern.slf4j.Slf4j;
import net.danil.dto.TestResult;
import net.danil.generated.junit.Testsuite;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
public class JavaTester extends Tester {
    public JavaTester(DockerClient dockerClient, @Value("${supreme-code.test-runner.container.ttk}") int ttk) {
        super(dockerClient, ttk);
    }

    @Override
    protected String solutionFilename() {
        return "/src/main/java/org/example/Solution.java";
    }

    @Override
    protected String containerName() {
        return "danil1digits0nly/sc-java-test:1.0";
    }

    @Override
    protected String reportPath() {
        return "/usr/app/target/surefire-reports/TEST-JunitTest.xml";
    }

    @Override
    protected TestResult.TestResultBuilder normalizeReport(Object parsedReport) {
        var testsuite = (Testsuite) parsedReport;
        return TestResult.builder()
                .tests(Integer.parseInt(testsuite.getTests()))
                .failures(Integer.parseInt(testsuite.getFailures()))
                .errors(Integer.parseInt(testsuite.getErrors()))
                .time(Double.parseDouble(testsuite.getTime()));
    }
}
