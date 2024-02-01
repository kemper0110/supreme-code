package net.danil;

import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.transport.DockerHttpClient;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class JavaRunner extends Runner{
    protected JavaRunner(DockerClientConfig clientConfig, DockerHttpClient httpClient) {
        super(clientConfig, httpClient);
    }

    @Override
    protected byte[] createArchive(String code) {
        try (InputStream inputStream = IOUtils.toInputStream(code, "UTF-8");
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(byteArrayOutputStream)) {

            TarArchiveEntry tarEntry = new TarArchiveEntry("App.java");
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

    @Override
    protected CreateContainerResponse createContainer() {
        // docker run -i --rm amazoncorretto:21-alpine sh -c 'Main.java && javac Main.java && java Main'
        return dockerClient.createContainerCmd("amazoncorretto:21-alpine")
                .withWorkingDir("/usr/app")
                .withCmd("sh", "-c", "javac App.java && java App")
                .exec();
    }
}
