package net.danil.web.controller;

import lombok.RequiredArgsConstructor;
import net.danil.web.security.UserInfo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class HelloController {
    @GetMapping("/hello")
    String hello() {
        return "hello!";
    }

    @GetMapping("/hello-user")
    public Mono<UserInfo> getAny(Authentication authentication) {
        return Mono.justOrEmpty(authentication).map(auth -> (UserInfo) auth.getPrincipal());
    }

    @GetMapping("/protected/hello")
    @PreAuthorize("isAuthenticated()")
    public Mono<String> getProtected() {
        return Mono.just("hello!");
    }
}
