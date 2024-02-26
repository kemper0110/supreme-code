package net.danil;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import net.danil.generated.junit.Testsuites;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class JavascriptTester extends Tester {
    public JavascriptTester(DockerClient dockerClient) {
        super(dockerClient);
    }

    @Override
    protected byte[] createArchive(String test, String code) {
        try (InputStream codeInputStream = IOUtils.toInputStream(code, "UTF-8");
             InputStream testInputStream = IOUtils.toInputStream(test, "UTF-8");
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(byteArrayOutputStream)) {

            TarArchiveEntry codeTarEntry = new TarArchiveEntry("index.js");
            codeTarEntry.setSize(codeInputStream.available());
            tarArchiveOutputStream.putArchiveEntry(codeTarEntry);
            IOUtils.copy(codeInputStream, tarArchiveOutputStream);
            tarArchiveOutputStream.closeArchiveEntry();

            TarArchiveEntry testTarEntry = new TarArchiveEntry("test.js");
            testTarEntry.setSize(testInputStream.available());
            tarArchiveOutputStream.putArchiveEntry(testTarEntry);
            IOUtils.copy(testInputStream, tarArchiveOutputStream);
            tarArchiveOutputStream.closeArchiveEntry();

            tarArchiveOutputStream.finish();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected CreateContainerResponse createContainer() {
        return dockerClient.createContainerCmd("sc-js-test-image").exec();
    }

    @Override
    protected String copyReport(String containerId) {
        try (
                final var testResultXmlStream = dockerClient.copyArchiveFromContainerCmd(containerId, "/usr/app/junit.xml").exec();
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
    protected TestResult parseReport(String xmlReport, String logs) {
        try {
            var jaxbContext = JAXBContext.newInstance("net.danil.generated.junit");
            var unmarshaller = jaxbContext.createUnmarshaller();
            var testsuites = (Testsuites) unmarshaller.unmarshal(IOUtils.toInputStream(xmlReport, "UTF-8"));

            return TestResult.builder()
                    .tests(Integer.parseInt(testsuites.getTests()))
                    .failures(Integer.parseInt(testsuites.getFailures()))
                    .errors(Integer.parseInt(testsuites.getErrors()))
                    .time(Double.parseDouble(testsuites.getTime()))
                    .xml(xmlReport)
                    .logs(logs)
                    .build();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
