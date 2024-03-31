package net.danil.runner;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JavaRunner extends Runner{
    protected JavaRunner(DockerClient dockerClient, @Value("${supreme-code.task-runner.container.ttk}") int ttk) {
        super(dockerClient, ttk);
    }

    @Override
    protected String codeFilename() {
        return "App.java";
    }

    @Override
    protected CreateContainerCmd createContainer() {
        // docker run -i --rm amazoncorretto:21-alpine sh -c 'Main.java && javac Main.java && java Main'
        return dockerClient.createContainerCmd("amazoncorretto:21-alpine")
                .withWorkingDir("/usr/app")
                .withCmd("sh", "-c", "javac App.java && java App");
    }
}
