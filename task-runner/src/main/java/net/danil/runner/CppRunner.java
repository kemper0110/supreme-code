package net.danil.runner;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CppRunner extends Runner {
    public CppRunner(DockerClient dockerClient, @Value("${supreme-code.task-runner.container.ttk}") int ttk) {
        super(dockerClient, ttk);
    }

    @Override
    protected CreateContainerCmd createContainer() {
        return dockerClient.createContainerCmd("gcc:13.2.0")
                .withWorkingDir("/usr/app")
                .withCmd("sh", "-c", "g++ -std=c++17 -o app app.cpp && ./app");
    }

    @Override
    protected String codeFilename() {
        return "app.cpp";
    }
}
