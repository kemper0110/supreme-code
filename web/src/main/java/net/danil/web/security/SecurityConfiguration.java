package net.danil.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfiguration {
    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter) {
        http.authorizeExchange(spec ->
                spec.pathMatchers("/hello/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN").anyExchange().authenticated());
        http.oauth2ResourceServer(spec ->
                spec.jwt(jwtSpec ->
                        jwtSpec.jwtAuthenticationConverter(jwtAuthenticationConverter))
        );
        http.oauth2Login(Customizer.withDefaults());

        return http.build();
    }
}

