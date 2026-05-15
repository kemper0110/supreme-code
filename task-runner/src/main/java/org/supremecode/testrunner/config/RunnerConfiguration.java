package org.supremecode.testrunner.config;

import com.github.dockerjava.api.DockerClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.supremecode.testrunner.ConfigurableRunner;

@Configuration
@EnableConfigurationProperties(LanguagesProperties.class)
public class RunnerConfiguration {
    private final LanguagesProperties languagesProperties;
    private final DockerClient dockerClient;
    private final int ttk;

    public RunnerConfiguration(LanguagesProperties languagesProperties, DockerClient dockerClient,
            @org.springframework.beans.factory.annotation.Value("${supreme-code.task-runner.container.ttk}") int ttk) {
        this.languagesProperties = languagesProperties;
        this.dockerClient = dockerClient;
        this.ttk = ttk;
    }

    @Bean
    public Iterable<ConfigurableRunner> configurableRunners() {
        return languagesProperties.getLanguages().values().stream()
                .map(config -> new ConfigurableRunner(dockerClient, ttk, config))
                .toList();
    }
}