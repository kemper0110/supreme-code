package net.danil.web.service;

import lombok.RequiredArgsConstructor;
import net.danil.web.dto.LoggedUser;
import net.danil.web.dto.UserLoginDto;
import net.danil.web.dto.UserRegisterDto;
import net.danil.web.model.User;
import net.danil.web.repository.UserRepository;
import net.danil.web.security.UserInfo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityService securityService;

    @Transactional
    public Mono<LoggedUser> login(UserLoginDto userLoginDto) {
        return Mono.justOrEmpty(userRepository.findByUsername(userLoginDto.username()))
                .flatMap(user -> {
                    if (!passwordEncoder.encode(userLoginDto.password()).equals(user.getPassword()))
                        return Mono.error(new RuntimeException("Invalid user password! INVALID_USER_PASSWORD"));
                    return Mono.just(
                            new LoggedUser(
                                    new UserInfo(user.getId(), user.getUsername()),
                                    securityService.generateAccessToken(user).toBuilder()
                                            .userId(user.getId())
                                            .build()
                            )
                    );
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid user, " + userLoginDto.username() + " is not registered. INVALID_USERNAME")));
    }

    public Mono<LoggedUser> register(UserRegisterDto userRegisterDto) {
        final var u = new User(null, userRegisterDto.username(), passwordEncoder.encode(userRegisterDto.password()), null, null);
        return Mono.justOrEmpty(userRepository.save(u))
                .flatMap(user -> Mono.just(
                        new LoggedUser(
                                new UserInfo(user.getId(), user.getUsername()),
                                securityService.generateAccessToken(user).toBuilder()
                                        .userId(user.getId())
                                        .build()
                        )
                ))
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid user, " + userRegisterDto.username() + " is not registered. INVALID_USERNAME")));
    }
}
