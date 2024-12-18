package net.danil.runner;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.danil.event.ErrorEvent;
import net.danil.event.LogEvent;
import net.danil.event.RunnerEvent;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
public abstract class Runner {
    protected final DockerClient dockerClient;
    protected final int ttk;

    protected abstract String codeFilename();

    protected byte[] createArchive(String code) {
        try (InputStream inputStream = IOUtils.toInputStream(code, "UTF-8");
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(byteArrayOutputStream)) {

            TarArchiveEntry tarEntry = new TarArchiveEntry(codeFilename());
            tarEntry.setSize(inputStream.available());
            tarArchiveOutputStream.putArchiveEntry(tarEntry);
            IOUtils.copy(inputStream, tarArchiveOutputStream);
            tarArchiveOutputStream.closeArchiveEntry();
            tarArchiveOutputStream.finish();

            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract CreateContainerCmd createContainer();

    @RequiredArgsConstructor
    class WaitCallback implements ResultCallback<WaitResponse> {
        protected final FluxSink<RunnerEvent> sink;
        protected final String containerId;
        @Override
        public void onStart(Closeable closeable) {

        }

        @Override
        public void onNext(WaitResponse waitResponse) {
            System.out.println(waitResponse.toString());
        }

        @Override
        public void onError(Throwable throwable) {
            sink.next(new ErrorEvent(throwable.getMessage()));
            sink.complete();
        }

        @Override
        public void onComplete() {
            // todo: to slf4j
            System.out.println("Wait completed");
            System.out.println("Runner is exiting");
            sink.complete();
        }

        @Override
        public void close() throws IOException {
            sink.complete();
        }
    }

    public Flux<RunnerEvent> run(String code) {
        final var cmd = createContainer();
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

        final var container = cmd.exec();
        final var containerId = container.getId();
        final var archive = createArchive(code);

        dockerClient.copyArchiveToContainerCmd(containerId)
                .withTarInputStream(new ByteArrayInputStream(archive))
                .withRemotePath("/usr/app")
                .exec();

        dockerClient.startContainerCmd(containerId).exec();

        return Flux.create(sink -> {
            dockerClient.logContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withFollowStream(true)
                    .exec(new LogCallback(sink, containerId));

            dockerClient.waitContainerCmd(container.getId()).exec(new WaitCallback(sink, containerId));

            sink.onDispose(() -> {
                try {
                    dockerClient.killContainerCmd(containerId).exec();
                    log.info("container({}): killed after timeout", containerId.substring(0, 8));
                } catch (NotFoundException e) {
                    log.info("container({}): not found to kill {}, maybe already killed", containerId.substring(0, 8), e.getMessage());
                } finally {
                    dockerClient.removeContainerCmd(containerId).exec();
                    System.out.println("Removed container");
                }
            });

            CompletableFuture.delayedExecutor(ttk, TimeUnit.MILLISECONDS).execute(sink::complete);
        });
    }

    @RequiredArgsConstructor
    class LogCallback implements ResultCallback<Frame> {
        protected final FluxSink<RunnerEvent> sink;
        protected final String containerId;
        @Override
        public void onStart(Closeable closeable) {
            System.out.println("start logging");
        }

        @Override
        public void onNext(Frame frame) {
            final var message = new String(frame.getPayload());
            sink.next(new LogEvent(message));
        }

        @Override
        public void onError(Throwable throwable) {
            sink.next(new ErrorEvent(throwable.getMessage()));
            sink.complete();
        }

        @Override
        public void onComplete() {
            System.out.println("complete logging");
            sink.complete();
        }

        @Override
        public void close() throws IOException {
            sink.complete();
        }
    }
}
