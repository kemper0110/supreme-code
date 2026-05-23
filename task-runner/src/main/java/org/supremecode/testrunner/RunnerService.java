package org.supremecode.testrunner;

import org.supremecode.testrunner.event.RunnerEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Service
public class RunnerService {
    private final Map<Language, Runner> runners;

    public RunnerService(List<ConfigurableRunner> configurableRunners) {
        this.runners = configurableRunners.stream().collect(java.util.stream.Collectors.toMap(ConfigurableRunner::getLanguage, r -> r));
    }

    public Flux<RunnerEvent> run(RunnerRequest request) {
        final var runner = runners.get(request.language());
        if (runner == null) {
            return Flux.error(new IllegalArgumentException("Unsupported language: " + request.language()));
        }
        return runner.run(request.code());
    }
}
