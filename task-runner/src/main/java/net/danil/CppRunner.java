package net.danil;

import java.io.*;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.WaitResponse;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;

public class CppRunner extends Runner {
    public CppRunner(DockerClientConfig clientConfig, DockerHttpClient httpClient) {
        super(clientConfig, httpClient);
    }

    @Override
    protected CreateContainerResponse createContainer() {
        return dockerClient.createContainerCmd("gcc:13.2.0")
                .withWorkingDir("/usr/app")
                .withCmd("sh", "-c", "g++ -std=c++17 -o app app.cpp && ./app")
                .exec();
    }

    @Override
    protected byte[] createArchive(String code) {
        try (InputStream inputStream = IOUtils.toInputStream(code, "UTF-8");
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(byteArrayOutputStream)) {

            TarArchiveEntry tarEntry = new TarArchiveEntry("app.cpp");
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
}
