package net.danil.web.security;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.Predicate;
/**
 * WebSecurityConfig class
 *
 * @author Erik Amaru Ortiz
 */
@Configuration
@EnableReactiveMethodSecurity
@EnableWebFluxSecurity
public class WebSecurityConfig {
    private final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.cookie-name}")
    private String cookieName;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, AuthenticationManager authManager) {
        http.authorizeExchange(spec -> spec.pathMatchers("/**").permitAll());
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);
        http.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable);
        http.formLogin(ServerHttpSecurity.FormLoginSpec::disable);
        http.exceptionHandling(spec -> {
            spec.authenticationEntryPoint((swe, e) -> {
                logger.info("[1] Authentication error: Unauthorized[401]: " + e.getMessage());
                return Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED));
            });
            spec.accessDeniedHandler((swe, e) -> {
                logger.info("[2] Authentication error: Access Denied[401]: " + e.getMessage());
                return Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN));
            });
        });
        http.addFilterAt(bearerAuthenticationFilter(authManager), SecurityWebFiltersOrder.AUTHENTICATION);
        http.addFilterAt(cookieAuthenticationFilter(authManager), SecurityWebFiltersOrder.AUTHENTICATION);
        return http.build();
    }

    /**
     * Spring security works by filter chaining.
     * We need to add a JWT CUSTOM FILTER to the chain.
     *
     * what is AuthenticationWebFilter:
     *
     *  A WebFilter that performs authentication of a particular request. An outline of the logic:
     *  A request comes in and if it does not match setRequiresAuthenticationMatcher(ServerWebExchangeMatcher),
     *  then this filter does nothing and the WebFilterChain is continued.
     *  If it does match then... An attempt to convert the ServerWebExchange into an Authentication is made.
     *  If the result is empty, then the filter does nothing more and the WebFilterChain is continued.
     *  If it does create an Authentication...
     *  The ReactiveAuthenticationManager specified in AuthenticationWebFilter(ReactiveAuthenticationManager) is used to perform authentication.
     *  If authentication is successful, ServerAuthenticationSuccessHandler is invoked and the authentication is set on ReactiveSecurityContextHolder,
     *  else ServerAuthenticationFailureHandler is invoked
     *
     */
    AuthenticationWebFilter bearerAuthenticationFilter(AuthenticationManager authManager) {
        AuthenticationWebFilter bearerAuthenticationFilter = new AuthenticationWebFilter(authManager);
        bearerAuthenticationFilter.setAuthenticationConverter(new ServerHttpBearerAuthenticationConverter(new JwtVerifyHandler(jwtSecret)));
        bearerAuthenticationFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/**"));

        return bearerAuthenticationFilter;
    }

    AuthenticationWebFilter cookieAuthenticationFilter(AuthenticationManager authManager) {
        AuthenticationWebFilter cookieAuthenticationFilter = new AuthenticationWebFilter(authManager);
        cookieAuthenticationFilter.setAuthenticationConverter(new ServerHttpCookieAuthenticationConverter(new JwtVerifyHandler(jwtSecret), cookieName));
        cookieAuthenticationFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/**"));

        return cookieAuthenticationFilter;
    }
}


@RequiredArgsConstructor
class ServerHttpCookieAuthenticationConverter implements Function<ServerWebExchange, Mono<Authentication>> {
    private final JwtVerifyHandler jwtVerifier;
    private final String cookieName;
    @Override
    public Mono<Authentication> apply(ServerWebExchange serverWebExchange) {
        return Mono.justOrEmpty(serverWebExchange)
                .flatMap(this::extract)
                .flatMap(jwtVerifier::check)
                .flatMap(CurrentUserAuthenticationBearer::create);
    }

    public Mono<String> extract(ServerWebExchange serverWebExchange) {
        var cookies = serverWebExchange.getRequest()
                .getCookies();
        var cookieSes = cookies.getFirst(cookieName);

        return cookieSes != null
                ? Mono.justOrEmpty(cookieSes.getValue())
                : Mono.empty();
    }
}

@RequiredArgsConstructor
class ServerHttpBearerAuthenticationConverter implements Function<ServerWebExchange, Mono<Authentication>> {
    private static final String BEARER = "Bearer ";
    private static final Predicate<String> matchBearerLength = authValue -> authValue.length() > BEARER.length();
    private static final Function<String, Mono<String>> isolateBearerValue = authValue -> Mono.justOrEmpty(authValue.substring(BEARER.length()));
    private final JwtVerifyHandler jwtVerifier;

    @Override
    public Mono<Authentication> apply(ServerWebExchange serverWebExchange) {
        return Mono.justOrEmpty(serverWebExchange)
                .flatMap(ServerHttpBearerAuthenticationConverter::extract)
                .filter(matchBearerLength)
                .flatMap(isolateBearerValue)
                .flatMap(jwtVerifier::check)
                .flatMap(CurrentUserAuthenticationBearer::create);
    }

    public static Mono<String> extract(ServerWebExchange serverWebExchange) {
        return Mono.justOrEmpty(serverWebExchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION));
    }
}