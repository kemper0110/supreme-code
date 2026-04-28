package org.supremecode.web.user.seeding;

import lombok.RequiredArgsConstructor;
import org.supremecode.web.domain.User;
import org.supremecode.web.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    Logger logger = LoggerFactory.getLogger(UserSeeder.class);

    @Override
    public void run(String... args) throws Exception {
        if(userRepository.count() > 0) return;

        final var user = new User();
        user.setId(1L);
        user.setUsername("root");
//        user.setPassword(passwordEncoder.encode("toor"));
        final var saved = userRepository.save(user);
        if(saved != null) {
            logger.info("created root user {}", saved);
        } else {
            logger.warn("root user not created");
        }
    }
}
