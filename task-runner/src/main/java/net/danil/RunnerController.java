package net.danil;

import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("/api/playground")
@RequiredArgsConstructor
public class RunnerController {
    private final RunnerService runnerService;

    @PostMapping
    public Flux<ServerSentEvent<?>> run(@RequestBody RunnerRequest request) {
        return runnerService.run(request).map(evt -> ServerSentEvent.builder()
                .event(evt.getEventType())
                .data(evt.getMessage().toString())
                .build());
    }
}
