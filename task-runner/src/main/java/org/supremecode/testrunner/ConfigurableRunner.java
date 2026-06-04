package org.supremecode.testrunner;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import lombok.extern.slf4j.Slf4j;
import org.supremecode.testrunner.config.LanguageConfig;

@Slf4j
public class ConfigurableRunner extends Runner {
    private final LanguageConfig config;

    public ConfigurableRunner(DockerClient dockerClient, int ttk, LanguageConfig config) {
        super(dockerClient, ttk);
        this.config = config;
    }

    public Language getLanguage() {
        return config.getLanguage();
    }

    @Override
    protected String codeFilename() {
        return config.getCodeFilename();
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