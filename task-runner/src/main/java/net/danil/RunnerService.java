package net.danil;

import lombok.RequiredArgsConstructor;
import net.danil.event.RunnerEvent;
import net.danil.runner.CppRunner;
import net.danil.runner.JavaRunner;
import net.danil.runner.JavascriptRunner;
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
