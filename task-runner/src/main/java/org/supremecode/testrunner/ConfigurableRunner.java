package org.supremecode.testrunner;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.supremecode.shared.RunnerConfig;
import org.supremecode.testrunner.config.LanguageConfig;

@Slf4j
public class ConfigurableRunner extends Runner {
    private final RunnerConfig config;
    @Getter
    private final String language;

    public ConfigurableRunner(DockerClient dockerClient, int ttk, String language, RunnerConfig config) {
        super(dockerClient, ttk);
        this.config = config;
        this.language = language;
    }

    @Override
    protected String codeFilename() {
        return config.getFilePath();
    }

    @Override
    protected CreateContainerCmd createContainer() {
        var container = dockerClient.createContainerCmd(config.getImage())
                .withWorkingDir("/usr/app");
        if (config.getCmd() != null)
            container = container.withCmd(config.getCmd());
        return container;
    }
}