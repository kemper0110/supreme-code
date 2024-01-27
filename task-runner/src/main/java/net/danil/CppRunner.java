package net.danil;

import java.io.*;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.WaitResponse;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;

public class CppRunner implements Function<String, String> {
    DockerClientConfig config;
    DockerHttpClient httpClient;
    public CppRunner() {
        config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://localhost:2375")
                .build();

        httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

    }
    @Override
    public String apply(String code) {

        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
        final var container = dockerClient.createContainerCmd("gcc:4.9")
                .withWorkingDir("/usr/app")
                .withCmd("sh", "-c", "g++ -o app app.cpp && ./app")
                .exec();

        CountDownLatch cancellationLatch = new CountDownLatch(2);


        try (InputStream inputStream = IOUtils.toInputStream(code, "UTF-8");
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(byteArrayOutputStream)) {

            TarArchiveEntry tarEntry = new TarArchiveEntry("app.cpp");
            tarEntry.setSize(inputStream.available());
            tarArchiveOutputStream.putArchiveEntry(tarEntry);
            IOUtils.copy(inputStream, tarArchiveOutputStream);
            tarArchiveOutputStream.closeArchiveEntry();
            tarArchiveOutputStream.finish();

            dockerClient.copyArchiveToContainerCmd(container.getId())
                    .withTarInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()))
                    .withRemotePath("/usr/app")
                    .exec();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
        while(true) {
            try {
                if(cancellationLatch.await(1, TimeUnit.SECONDS))
                    break;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("thread: waited 1 second");
        }

        System.out.println("CppRunner is exiting");
        return builder.toString();
    }
}
