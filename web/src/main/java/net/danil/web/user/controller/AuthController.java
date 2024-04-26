package net.danil.web.user.controller;

import lombok.RequiredArgsConstructor;
import net.danil.web.user.dto.UserLoginDto;
import net.danil.web.user.dto.UserRegisterDto;
import net.danil.web.user.service.AuthService;
import net.danil.web.user.service.SecurityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;


/**
 * Реализовано 2 варианта аутентификации
 * - jwt токен устанавливается в http-only cookie
 * - jwt токен возвращается результатом запроса и клиент сам решит, как его хранить
 * вариант аутентификации определяется по наличию заголовка X-No-Cookie
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {
    private final SecurityService securityService;
    private final AuthService authService;

    record LoggedUserResponse(Long id, String username, Long maxAge) {

    }

    @PostMapping(value = "/login")
    public Mono<ResponseEntity<Object>> login(@RequestBody UserLoginDto dto) {
        return authService.login(dto)
                .map(loggedUser -> {
                            var userInfo = loggedUser.getUserInfo();
                            var tokenInfo = loggedUser.getTokenInfo();
                            return ResponseEntity.ok()
                                    .header("Set-Cookie", securityService.makeJwtCookie(tokenInfo))
                                    .body(Map.of(
                                            "user", new LoggedUserResponse(userInfo.getId(), userInfo.getUsername(),
                                                    tokenInfo.getExpiresAt().getTime() - tokenInfo.getIssuedAt().getTime()
                                            )
                                    ));
                        }
                );

    }

    @PostMapping("/register")
    public Mono<ResponseEntity<Object>> register(@RequestBody UserRegisterDto dto) {
        return authService.register(dto)
                .map(loggedUser -> {
                            var userInfo = loggedUser.getUserInfo();
                            var tokenInfo = loggedUser.getTokenInfo();
                            return ResponseEntity.ok()
                                    .header("Set-Cookie", securityService.makeJwtCookie(tokenInfo))
                                    .body(Map.of(
                                            "user", new LoggedUserResponse(userInfo.getId(), userInfo.getUsername(),
                                                    tokenInfo.getExpiresAt().getTime() - tokenInfo.getIssuedAt().getTime()
                                            )
                                    ));
                        }
                );
    }

//    @PostMapping(name = "/register", headers = {"X-No-Cookie"})
//    public Mono<ResponseEntity<Object>> registerNoCookie(@RequestBody UserRegisterDto userDto) {
//        return userService.createUser(new User(null, userDto.username(), userDto.password(), null, null))
//                .flatMap(user -> securityService.authenticate(user.getUsername(), user.getPassword()))
//                .map(tokenInfo -> ResponseEntity.ok()
//                        .body(AuthResultDto.builder()
//                                .token(tokenInfo.getToken())
//                                .issuedAt(tokenInfo.getIssuedAt())
//                                .expiresAt(tokenInfo.getExpiresAt())
//                                .userId(tokenInfo.getUserId())
//                                .build()
//                        )
//                );
//    }
//
//    @PostMapping(value = "/login", headers = {"X-No-Cookie"})
//    public Mono<ResponseEntity<Object>> loginNoCookie(@RequestBody UserLoginDto dto) {
//        return securityService.authenticate(dto.username(), dto.password())
//                .map(tokenInfo ->
//                        ResponseEntity.ok()
//                                .body(AuthResultDto.builder()
//                                        .token(tokenInfo.getToken())
//                                        .issuedAt(tokenInfo.getIssuedAt())
//                                        .expiresAt(tokenInfo.getExpiresAt())
//                                        .userId(tokenInfo.getUserId())
//                                        .build()
//                                )
//                );
//    }
}
