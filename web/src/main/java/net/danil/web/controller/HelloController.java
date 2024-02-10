package net.danil.web.controller;

import lombok.RequiredArgsConstructor;
import net.danil.web.model.User;
import net.danil.web.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class HelloController {
    private final UserRepository userRepository;
    @GetMapping("/hello")
    List<User> hello() {
        return userRepository.findAll();
    }
}
