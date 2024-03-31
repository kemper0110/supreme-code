package net.danil;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;


@RestController
@RequiredArgsConstructor
public class RunnerController {
    private final RunnerService runnerService;
    @PostMapping(value = "/submit", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> run(@RequestBody RunnerRequest request) {
        return runnerService.run(request);
    }
}
