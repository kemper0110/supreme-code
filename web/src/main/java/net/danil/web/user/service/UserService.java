package net.danil.web.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.danil.web.domain.User;
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
        final var uu = userRepository.save(u);
        log.info("Created new user with ID = " + uu.getId());
        return Mono.just(uu);
    }

    public Mono<User> getUser(Long userId) {
        return Mono.justOrEmpty(userRepository.findById(userId));
    }
    public Mono<User> getUserByUsername(String username) {
        return Mono.justOrEmpty(userRepository.findByUsername(username));
    }
}
