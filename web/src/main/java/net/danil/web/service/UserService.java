package net.danil.web.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.danil.web.model.User;
import net.danil.web.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<User> createUser(User user) {
        final var u = user.toBuilder().password(passwordEncoder.encode(user.getPassword())).build();
        return userRepository.save(u).map(uu -> {
            log.info("Created new user with ID = " + uu.getId());
            return uu;
        });
    }

    public Mono<User> getUser(Long userId) {
        return userRepository.findById(userId);
    }
    public Mono<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
