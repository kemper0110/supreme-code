package org.supremecode.testrunner;

import org.supremecode.testrunner.event.RunnerEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class RunnerService {
    private final RunnerRegistry runnerRegistry;

    public RunnerService(RunnerRegistry runnerRegistry) {
        this.runnerRegistry = runnerRegistry;
    }

    public Flux<RunnerEvent> run(RunnerRequest request) {
        final var runner = runnerRegistry.get(request.language());
        if (runner == null) {
            return Flux.error(new IllegalArgumentException("Unsupported language: " + request.language()));
        }
        return runner.run(request.code());
    }
}
