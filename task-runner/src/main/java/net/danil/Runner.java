package net.danil;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.WaitResponse;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.transport.DockerHttpClient;

import java.io.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class Runner {
    protected final DockerClientConfig clientConfig;
    protected final DockerHttpClient httpClient;
    protected final DockerClient dockerClient;

    protected Runner(DockerClientConfig clientConfig, DockerHttpClient httpClient) {
        this.clientConfig = clientConfig;
        this.httpClient = httpClient;
        this.dockerClient = DockerClientImpl.getInstance(clientConfig, httpClient);
    }

    protected abstract byte[] createArchive(String code);
    protected abstract CreateContainerResponse createContainer();

    public String run(String code) {
        final var container = createContainer();
        final var archive = createArchive(code);

        dockerClient.copyArchiveToContainerCmd(container.getId())
                .withTarInputStream(new ByteArrayInputStream(archive))
                .withRemotePath("/usr/app")
                .exec();

        dockerClient.startContainerCmd(container.getId()).exec();

        CountDownLatch cancellationLatch = new CountDownLatch(2);

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
                        cancellationLatch.countDown();
                    }

                    @Override
                    public void close() throws IOException {

                    }
                });


        dockerClient.waitContainerCmd(container.getId()).exec(new ResultCallback<WaitResponse>() {
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
            public void onComplete() {
                System.out.println("Wait completed");
                cancellationLatch.countDown();
            }

            @Override
            public void close() throws IOException {

            }
        });
        while (true) {
            try {
                if (cancellationLatch.await(1, TimeUnit.SECONDS))
                    break;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("thread: waited 1 second");
        }
        System.out.println("Run don");
        dockerClient.removeContainerCmd(container.getId()).exec();
        System.out.println("Removed container");

        System.out.println("CppRunner is exiting");
        return builder.toString();
    }
}
