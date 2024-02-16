package net.danil;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.WaitResponse;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.transport.DockerHttpClient;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.function.Consumer;

public abstract class Tester {
    protected final DockerClientConfig clientConfig;
    protected final DockerHttpClient httpClient;
    protected final DockerClient dockerClient;

    public Tester(DockerClientConfig clientConfig, DockerHttpClient httpClient) {
        this.clientConfig = clientConfig;
        this.httpClient = httpClient;
        this.dockerClient = DockerClientImpl.getInstance(clientConfig, httpClient);
    }

    protected abstract byte[] createArchive(String test, String code);
    protected abstract CreateContainerResponse createContainer();
    protected abstract String copyReport(String containerId);
    protected abstract TestResult parseReport(String xmlReport, String logs);

    void test(String test, String code, Consumer<Object> resultCallback) {
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
                        System.out.println("start logging");
                    }

                    @Override
                    public void onNext(Frame frame) {
                        builder.append(new String(frame.getPayload()));
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        System.out.println(throwable.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("complete logging");
                    }

                    @Override
                    public void close() throws IOException {

                    }
                });


        dockerClient.waitContainerCmd(container.getId()).exec(new ResultCallback<WaitResponse>() {
            @Override
            public void onComplete() {
                System.out.println("Wait completed");
                try {
                    final var report = copyReport(container.getId());
                    final var result = parseReport(report, builder.toString());
                    resultCallback.accept(result);
                } catch (Exception e) {
                    resultCallback.accept(TestResult.builder().logs(builder.toString()).build());
                } finally {
                    dockerClient.removeContainerCmd(container.getId()).exec();
                    System.out.println("Removed container");
                    System.out.println("Tester is exiting " + container.getId());
                }
            }
            @Override
            public void onStart(Closeable closeable) {

            }

            @Override
            public void onNext(WaitResponse waitResponse) {
                System.out.println(waitResponse.toString());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println(throwable);
            }
            @Override
            public void close() throws IOException {

            }
        });
    }
}
