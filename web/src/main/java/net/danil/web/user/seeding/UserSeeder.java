package net.danil.web.user.seeding;

import lombok.RequiredArgsConstructor;
import net.danil.web.user.model.User;
import net.danil.web.user.repository.UserRepository;
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

        final var saved = userRepository.save(
                User.builder()
                        .id(1L)
                        .username("root")
                        .password(passwordEncoder.encode("toor"))
                        .image(null)
                        .build()
        );
        if(saved != null) {
            logger.info("created root user {}", saved);
        } else {
            logger.warn("root user not created");
        }
    }
}
