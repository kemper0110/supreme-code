//package net.danil;
//
//import com.github.dockerjava.api.command.CreateContainerResponse;
//import com.github.dockerjava.core.DockerClientConfig;
//import com.github.dockerjava.transport.DockerHttpClient;
//import org.springframework.stereotype.Service;
//
//@Service
//public class JavaTester extends Tester {
//    public JavaTester(DockerClientConfig clientConfig, DockerHttpClient httpClient) {
//        super(clientConfig, httpClient);
//    }
//
//    @Override
//    protected byte[] createArchive(String test, String code) {
//        return new byte[0];
//    }
//
//    @Override
//    protected CreateContainerResponse createContainer() {
//        return null;
//    }
//
//    @Override
//    protected String copyReport(String containerId) {
//        return null;
//    }
//
//    @Override
//    protected TestResult parseReport(String xmlReport, String logs) {
//        return null;
//    }
//}
