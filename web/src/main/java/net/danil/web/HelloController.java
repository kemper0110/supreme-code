package net.danil.web;

import lombok.RequiredArgsConstructor;
import net.danil.web.model.User;
import net.danil.web.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class HelloController {
    private final UserRepository userRepository;
    @GetMapping("/hello")
    Flux<User> hello() {
        return userRepository.findAll();
    }
}
