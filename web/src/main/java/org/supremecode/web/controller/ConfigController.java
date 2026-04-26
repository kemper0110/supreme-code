package org.supremecode.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.supremecode.web.domain.PlatformConfig;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/config")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class ConfigController {
    final PlatformConfig platformConfig;

    @GetMapping
    Mono<PlatformConfig> index() {
        return Mono.just(platformConfig);
    }
}
