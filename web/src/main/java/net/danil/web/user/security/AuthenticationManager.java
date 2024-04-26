package net.danil.web.user.security;

import net.danil.web.user.service.UserService;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * AuthenticationManager class
 * It is used in AuthenticationFilter.
 *
 * @author Erik Amaru Ortiz
 */
@Component
public class AuthenticationManager implements ReactiveAuthenticationManager {
    private final UserService userService;

    public AuthenticationManager(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        var principal = (UserInfo) authentication.getPrincipal();

        return userService.getUser(principal.getId()).map(user -> authentication);
    }
}
