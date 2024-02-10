package net.danil.web.controller;

import lombok.RequiredArgsConstructor;
import net.danil.web.dto.BasicUserDto;
import net.danil.web.model.User;
import net.danil.web.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Stream;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    private final UserRepository userRepository;

    @GetMapping
    Stream<BasicUserDto> index() {
        return userRepository.findAll().stream().map(BasicUserDto::fromUser);
    }

    @PostMapping
    BasicUserDto store(User user) {
        return BasicUserDto.fromUser(userRepository.save(user));
    }

    @PutMapping("/{id}")
    BasicUserDto update(User user) {
        return BasicUserDto.fromUser(userRepository.save(user));
    }

    @DeleteMapping("/{id}")
    void delete(@PathVariable Long id) {
        userRepository.deleteById(id);
    }
}
