package net.danil.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/api/playground")
@RequiredArgsConstructor
public class PlaygroundController {
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("task-runner", r -> r
                        .path("/api/playground")
                        .and()
                        .predicate(p -> p.getRequest().getMethod().matches("POST"))
                        .uri("http://localhost:8090/api/task")
                )
                .build();
    }
}
