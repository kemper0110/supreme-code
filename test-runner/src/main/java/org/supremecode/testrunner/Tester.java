package org.supremecode.testrunner;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.supremecode.pluginsdk.LanguageTester;
import org.supremecode.pluginsdk.result.TestExecutionResult;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.supremecode.testrunner.dto.TestResult;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.*;

@RequiredArgsConstructor
@Slf4j
public class Tester {
    protected final DockerClient dockerClient;
    protected final int ttk;
    protected final LanguageTester languageTester;

    protected byte[] createArchive(String tests, String code) {
        try (
                InputStream codeInputStream = IOUtils.toInputStream(code, "UTF-8");
                InputStream testsInputStream = IOUtils.toInputStream(tests, "UTF-8");
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                TarArchiveOutputStream tarOutput = new TarArchiveOutputStream(byteArrayOutputStream);
        ) {
            TarArchiveEntry testsTarEntry = new TarArchiveEntry(languageTester.testsPath());
            testsTarEntry.setSize(testsInputStream.available());
            tarOutput.putArchiveEntry(testsTarEntry);
            IOUtils.copy(testsInputStream, tarOutput);
            tarOutput.closeArchiveEntry();

            TarArchiveEntry codeTarEntry = new TarArchiveEntry(languageTester.solutionPath());
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

    protected CreateContainerResponse createContainer() {
        final var cmd = dockerClient.createContainerCmd(languageTester.imageName());
        cmd.withHostConfig(
                new HostConfig()
                        .withMemory(1024L * 1024L * 400L)
                        .withMemorySwap(1024L * 1024L * 1024L)
                        .withNanoCPUs(1_000_000_000L)
                        .withSecurityOpts(List.of("no-new-privileges"))
                        .withRestartPolicy(RestartPolicy.noRestart())
                        .withUlimits(
                                List.of(
                                        new Ulimit("nofile", 468L, 512L),
                                        new Ulimit("nproc", 58L, 64L)
                                )
                        )
                        .withCapDrop(Capability.NET_ADMIN)
                        .withNetworkMode("none")
        );
        cmd.withNetworkDisabled(true);
        return cmd.exec();
    }

    protected String copyReport(String containerId) {
        try (
                final var testResultXmlStream = dockerClient.copyArchiveFromContainerCmd(containerId, languageTester.reportPath()).exec();
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


    public CompletableFuture<TestResult> test(String tests, String code) {
        final var container = createContainer();
        final var containerId = container.getId();

        final var archive = createArchive(tests, code);

        dockerClient.copyArchiveToContainerCmd(containerId)
                .withTarInputStream(new ByteArrayInputStream(archive))
                .withRemotePath("/usr/app")
                .exec();

        dockerClient.startContainerCmd(containerId).exec();

        final var logCallback = new LogCallback(containerId);
        dockerClient.logContainerCmd(containerId)
                .withStdOut(true)
                .withStdErr(true)
                .withFollowStream(true)
                .exec(logCallback);

        final var timeoutCF = new SetTimeout().setTimeout(ttk);

        final var waitCF = new CompletableFuture<WaitResult>();
        dockerClient.waitContainerCmd(containerId)
                .exec(new WaitCallback(containerId, waitCF));

        return CompletableFuture.anyOf(timeoutCF, waitCF)
                .thenApply(result -> {
                    TestResult testResultBuilder;
                    final var logs = logCallback.getLogs();
                    if (result instanceof WaitResult waitResult) {
                        final var report = copyReport(containerId);
                        final var verdict = languageTester.verdict(report, waitResult.exitCode());
                        testResultBuilder = new TestResult(verdict.total(), verdict.failures(), verdict.errors(), verdict.solved(), waitResult.exitCode(), report, logs);
                    } else {
                        testResultBuilder = new TestResult(0, 0, 0, false, -1, "", logs);
                    }
                    try {
                        dockerClient.killContainerCmd(containerId).exec();
                        log.error("container({}): killed", containerId.substring(0, 8));
                    } catch (Exception e) {
                        log.error("container({}): not found to kill {}", containerId.substring(0, 8), e.getMessage());
                    }
                    try {
                        dockerClient.removeContainerCmd(containerId).exec();
                        log.error("container({}): removed", containerId.substring(0, 8));
                    } catch (Exception e) {
                        log.error("container({}): not found to remove {}", containerId.substring(0, 8), e.getMessage());
                    }
                    return testResultBuilder;
                });
    }
}
