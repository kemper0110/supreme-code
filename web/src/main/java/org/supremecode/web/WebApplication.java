package org.supremecode.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.supremecode.web.repository.SolutionRepository;
import org.supremecode.web.repository.SolutionResultRepository;
import org.supremecode.web.service.MinioPathService;
import org.supremecode.web.service.TestRunnerChannelService;

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan({"org.supremecode"})
@EnableTransactionManagement
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }

    @Bean
    public MinioPathService minioPathService() {
        return new MinioPathService();
    }

    @Bean
    public TestRunnerChannelService testRunnerChannelService(
            SolutionRepository solutionRepository,
            SolutionResultRepository solutionResultRepository
    ) {
        return new TestRunnerChannelService(solutionRepository, solutionResultRepository);
    }
}
