package org.supremecode.testrunner.config;

import com.github.dockerjava.api.DockerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.supremecode.shared.PlatformConfig;
import org.supremecode.testrunner.ConfigurableRunner;
import org.supremecode.testrunner.Language;
import org.supremecode.testrunner.Runner;
import org.supremecode.testrunner.RunnerRegistry;

import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class RunnerConfiguration {
    private final PlatformConfig platformConfig;
    private final DockerClient dockerClient;
    private final int ttk;

    public RunnerConfiguration(PlatformConfig platformConfig, DockerClient dockerClient,
                               @org.springframework.beans.factory.annotation.Value("${supreme-code.task-runner.container.ttk}") int ttk) {
        this.platformConfig = platformConfig;
        this.dockerClient = dockerClient;
        this.ttk = ttk;
    }

    @Bean
    public RunnerRegistry runnerRegistry() {
        final Map<String, Runner> runners = platformConfig.getLanguages().entrySet().stream()
                .filter(entry -> entry.getValue().getRunnerConfig() != null)
                .map(entry -> {
                    final var languageConfig = entry.getValue();
                    final var runnerConfig = languageConfig.getRunnerConfig();

                    return new ConfigurableRunner(
                            dockerClient,
                            ttk,
                            entry.getKey(),
                            runnerConfig
                    );
                })
                .collect(Collectors.toMap(ConfigurableRunner::getLanguage, runner -> (Runner) runner));

        return new RunnerRegistry(runners);
    }
}
