package net.danil;

import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.transport.DockerHttpClient;

public class JavaTester extends Tester {
    public JavaTester(DockerClientConfig clientConfig, DockerHttpClient httpClient) {
        super(clientConfig, httpClient);
    }
}
