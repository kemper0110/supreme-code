package net.danil;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.WaitResponse;
import lombok.RequiredArgsConstructor;
import net.danil.dto.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

@RequiredArgsConstructor
public abstract class Tester {
    private final Logger logger = LoggerFactory.getLogger(Tester.class);
    protected final DockerClient dockerClient;

    protected abstract byte[] createArchive(Path test, String code);
    protected abstract CreateContainerResponse createContainer();
    protected abstract String copyReport(String containerId);
    protected abstract TestResult.TestResultBuilder parseReport(String xmlReport);

    void test(Path test, String code, Consumer<TestResult.TestResultBuilder> resultCallback) {
        final var container = createContainer();
        final var archive = createArchive(test, code);

        dockerClient.copyArchiveToContainerCmd(container.getId())
                .withTarInputStream(new ByteArrayInputStream(archive))
                .withRemotePath("/usr/app")
                .exec();

        dockerClient.startContainerCmd(container.getId()).exec();

        StringBuilder builder = new StringBuilder();

        dockerClient.logContainerCmd(container.getId())
                .withStdOut(true)
                .withStdErr(true)
                .withFollowStream(true)
                .exec(new ResultCallback<Frame>() {
                    @Override
                    public void onStart(Closeable closeable) {
                        logger.debug("container({})-logging: started", container.getId());
                    }

                    @Override
                    public void onNext(Frame frame) {
                        builder.append(new String(frame.getPayload()));
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        logger.error("container({})-logging: error {}", container.getId(), throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        logger.debug("container({})-logging: completed", container.getId());
                    }

                    @Override
                    public void close() throws IOException {

                    }
                });


        dockerClient.waitContainerCmd(container.getId()).exec(new ResultCallback<WaitResponse>() {
            int statusCode = 0;
            @Override
            public void onComplete() {
                logger.debug("container({})-wait: complete", container.getId());
                String logs = builder.toString();
                try {
                    final var report = copyReport(container.getId());
                    final var result = parseReport(report).logs(logs).statusCode(statusCode);
                    resultCallback.accept(result);
                } catch (Exception e) {
                    resultCallback.accept(TestResult.builder().logs(logs).statusCode(statusCode));
                } finally {
                    dockerClient.removeContainerCmd(container.getId()).exec();
                    logger.debug("container({})-wait: removed container, exiting", container.getId());
                }
            }
            @Override
            public void onStart(Closeable closeable) {

            }

            @Override
            public void onNext(WaitResponse waitResponse) {
                statusCode = waitResponse.getStatusCode();
                logger.info("container({})-wait: status-code={}", container.getId(), waitResponse.getStatusCode());
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error("container({})-wait: error {}", container.getId(), throwable.getMessage());
            }
            @Override
            public void close() throws IOException {

            }
        });
    }
}
