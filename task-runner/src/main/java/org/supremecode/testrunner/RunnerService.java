package org.supremecode.testrunner;

import lombok.RequiredArgsConstructor;
import org.supremecode.testrunner.event.RunnerEvent;
import org.supremecode.testrunner.runner.CppRunner;
import org.supremecode.testrunner.runner.JavaRunner;
import org.supremecode.testrunner.runner.JavascriptRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class RunnerService {
    private final CppRunner cppRunner;
    private final JavaRunner javaRunner;
    private final JavascriptRunner javascriptRunner;

    public Flux<RunnerEvent> run(RunnerRequest request) {
        final var runner = switch (request.language()) {
            case Cpp -> cppRunner;
            case Java -> javaRunner;
            case Javascript -> javascriptRunner;
        };

        return runner.run(request.code());
    }
}
