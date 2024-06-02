package net.danil;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.danil.dto.TestResult;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.*;

@RequiredArgsConstructor
@Slf4j
public abstract class Tester {
    protected final DockerClient dockerClient;
    protected final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    protected final int ttk;

    protected abstract String solutionFilename();

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

            TarArchiveEntry codeTarEntry = new TarArchiveEntry(solutionFilename());
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

    protected abstract String containerName();

    protected CreateContainerResponse createContainer() {
        final var cmd = dockerClient.createContainerCmd(containerName());
        cmd.withHostConfig(
                new HostConfig()
                        .withMemory(1024L * 1024L * 400L)
                        .withMemorySwap(1024L * 1024L * 1024L)
                        .withNanoCPUs(1_000_000_000L)
                        .withSecurityOpts(List.of("no-new-privileges"))
                        .withRestartPolicy(RestartPolicy.noRestart())
                        .withUlimits(
                                List.of(
                                        new Ulimit("nofile", 112L, 128L),
                                        new Ulimit("nproc", 10L, 16L)
                                )
                        )
                        .withCapDrop(Capability.NET_ADMIN)
                        .withNetworkMode("none")
        );
        cmd.withNetworkDisabled(true);
        return cmd.exec();
    }

    protected abstract String reportPath();

    protected String copyReport(String containerId) {
        try (
                final var testResultXmlStream = dockerClient.copyArchiveFromContainerCmd(containerId, reportPath()).exec();
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

    protected abstract TestResult.TestResultBuilder normalizeReport(Object parsedReport);

    protected TestResult.TestResultBuilder parseReport(String xmlReport) {
        try {
            var jaxbContext = JAXBContext.newInstance("net.danil.generated.junit");
            var unmarshaller = jaxbContext.createUnmarshaller();
            final var object = unmarshaller.unmarshal(IOUtils.toInputStream(xmlReport, "UTF-8"));
            return normalizeReport(object)
                    .xml(xmlReport);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    @RequiredArgsConstructor
    protected static class LogCallback implements ResultCallback<Frame> {
        final protected String containerId;

        @Getter
        final protected StringBuilder builder = new StringBuilder();

        @Override
        public void onStart(Closeable closeable) {
            log.debug("container({})-logging: started", containerId);
        }

        @Override
        public void onNext(Frame frame) {
            builder.append(new String(frame.getPayload()));
        }

        @Override
        public void onError(Throwable throwable) {
            log.error("container({})-logging: error {}", containerId, throwable.getMessage());
        }

        @Override
        public void onComplete() {
            log.debug("container({})-logging: completed", containerId);
        }

        @Override
        public void close() throws IOException {

        }
    }

    protected LogCallback logCallback(String containerId) {
        return new LogCallback(containerId);
    }

    @RequiredArgsConstructor
    protected class WaitCallback implements ResultCallback<WaitResponse> {
        int statusCode = 0;
        final protected String containerId;
        final protected StringBuilder builder;
        final protected CompletableFuture<TestResult.TestResultBuilder> resultFuture;

        @Override
        public void onComplete() {
            log.debug("container({})-wait: complete", containerId);
            String logs = builder.toString();
            try {
                final var report = copyReport(containerId);
                final var result = parseReport(report).logs(logs).statusCode(statusCode);
                resultFuture.complete(result);
            } catch (Exception e) {
                resultFuture.complete(TestResult.builder().logs(logs).statusCode(statusCode));
            }
        }

        @Override
        public void onStart(Closeable closeable) {

        }

        @Override
        public void onNext(WaitResponse waitResponse) {
            statusCode = waitResponse.getStatusCode();
            log.info("container({})-wait: status-code={}", containerId, waitResponse.getStatusCode());
        }

        @Override
        public void onError(Throwable throwable) {
            log.error("container({})-wait: error {}", containerId, throwable.getMessage());
        }

        @Override
        public void close() throws IOException {

        }
    }

    protected WaitCallback waitCallback(String containerId, StringBuilder builder, CompletableFuture<TestResult.TestResultBuilder> resultCallback) {
        return new WaitCallback(containerId, builder, resultCallback);
    }

    public CompletableFuture<TestResult.TestResultBuilder> test(Path test, String code) {
        final var container = createContainer();
        final var containerId = container.getId();

        final var archive = createArchive(test, code);

        dockerClient.copyArchiveToContainerCmd(containerId)
                .withTarInputStream(new ByteArrayInputStream(archive))
                .withRemotePath("/usr/app")
                .exec();

        dockerClient.startContainerCmd(containerId).exec();

        final var logCallback = logCallback(containerId);

        dockerClient.logContainerCmd(containerId)
                .withStdOut(true)
                .withStdErr(true)
                .withFollowStream(true)
                .exec(logCallback);


        final var timeoutCF = new CompletableFuture<Void>();
        scheduler.schedule(() -> timeoutCF.complete(null), ttk, TimeUnit.MILLISECONDS);

        final var waitCF = new CompletableFuture<TestResult.TestResultBuilder>();
        dockerClient.waitContainerCmd(containerId).exec(
                waitCallback(containerId, logCallback.builder, waitCF)
        );

        return CompletableFuture.anyOf(timeoutCF, waitCF)
                .thenApply(result -> {
                    if (result == null) {
                        try {
                            dockerClient.killContainerCmd(containerId).exec();
                            log.error("container({}): killed after timeout", containerId.substring(0, 8));
                        } catch (NotFoundException e) {
                            log.error("container({}): not found to kill {}", containerId.substring(0, 8), e.getMessage());
                        }
                    } else {
                        timeoutCF.cancel(true);
                    }
                    dockerClient.removeContainerCmd(containerId).exec();
                    log.debug("container({})-wait: removed container, exiting", containerId);
                    return waitCF;
                })
                .thenCompose(completedFuture -> completedFuture);
    }
}
