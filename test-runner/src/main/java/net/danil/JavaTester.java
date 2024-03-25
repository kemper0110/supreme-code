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
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
public class JavaTester extends Tester {
    public JavaTester(DockerClient dockerClient) {
        super(dockerClient);
    }

    @Override
    protected byte[] createArchive(Path test, String code) {
        try (
                InputStream codeInputStream = IOUtils.toInputStream(code, "UTF-8");
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                TarArchiveOutputStream tarOutput = new TarArchiveOutputStream(byteArrayOutputStream);
                var files = Files.walk(test, FileVisitOption.FOLLOW_LINKS);
        ) {
            files.forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    try {
                        TarArchiveEntry entry = new TarArchiveEntry(filePath.toFile(), test.relativize(filePath).toString());
                        tarOutput.putArchiveEntry(entry);
                        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath.toFile()))) {
                            IOUtils.copy(bis, tarOutput);
                            tarOutput.closeArchiveEntry();
                        }
                    } catch (IOException e) {
                        log.error("test repository copy fail: {}", e.getMessage());
                    }
                }
            });

            TarArchiveEntry codeTarEntry = new TarArchiveEntry("/src/main/java/org/example/Solution.java");
            codeTarEntry.setSize(codeInputStream.available());
            tarOutput.putArchiveEntry(codeTarEntry);
            IOUtils.copy(codeInputStream, tarOutput);
            tarOutput.closeArchiveEntry();

            tarOutput.finish();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected CreateContainerResponse createContainer() {
        return dockerClient.createContainerCmd("danil1digits0nly/sc-java-test:1.0").exec();
    }

    @Override
    protected String copyReport(String containerId) {
        try (
                final var testResultXmlStream = dockerClient.copyArchiveFromContainerCmd(containerId, "/usr/app/target/surefire-reports/TEST-JunitTest.xml").exec();
                final var tarArchiveInputStream = new TarArchiveInputStream(testResultXmlStream);
                final var byteArrayOutputStream = new ByteArrayOutputStream();
        ) {
            tarArchiveInputStream.getNextTarEntry();
            IOUtils.copy(tarArchiveInputStream, byteArrayOutputStream);
            return byteArrayOutputStream.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected TestResult.TestResultBuilder parseReport(String xmlReport) {
        try {
            var jaxbContext = JAXBContext.newInstance("net.danil.generated.junit");
            var unmarshaller = jaxbContext.createUnmarshaller();
            var testsuite = (Testsuite) unmarshaller.unmarshal(IOUtils.toInputStream(xmlReport, "UTF-8"));

            return TestResult.builder()
                    .tests(Integer.parseInt(testsuite.getTests()))
                    .failures(Integer.parseInt(testsuite.getFailures()))
                    .errors(Integer.parseInt(testsuite.getErrors()))
                    .time(Double.parseDouble(testsuite.getTime()))
                    .xml(xmlReport);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
