package net.danil;

import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.transport.DockerHttpClient;

public abstract class Tester {
    protected final DockerClientConfig clientConfig;
    protected final DockerHttpClient httpClient;
    public Tester(DockerClientConfig clientConfig, DockerHttpClient httpClient) {
        this.clientConfig = clientConfig;
        this.httpClient = httpClient;
    }
}
