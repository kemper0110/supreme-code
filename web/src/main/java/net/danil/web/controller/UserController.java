package net.danil.web.controller;

import lombok.RequiredArgsConstructor;
import net.danil.web.model.User;
import net.danil.web.repository.UserRepository;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    private final UserRepository userRepository;

    @GetMapping
    List<User> index() {
        return userRepository.findAll();
    }

    @PostMapping
    User store(User user) {
        return userRepository.save(user);
    }

    @PutMapping("/{id}")
    User update(User user) {
        return userRepository.save(user);
    }

    @DeleteMapping("/{id}")
    void delete(@PathVariable Long id) {
        userRepository.deleteById(id);
    }
}
