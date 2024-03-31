package net.danil.runner;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JavascriptRunner extends Runner{
    protected JavascriptRunner(DockerClient dockerClient, @Value("${supreme-code.task-runner.container.ttk}") int ttk) {
        super(dockerClient, ttk);
    }

    @Override
    protected String codeFilename() {
        return "app.js";
    }

    @Override
    protected CreateContainerCmd createContainer() {
        // docker run --rm node:20 sh -c 'node app.js'
        return dockerClient.createContainerCmd("node:20-alpine")
                .withWorkingDir("/usr/app")
                .withCmd("sh", "-c", "node app.js");
    }
}
