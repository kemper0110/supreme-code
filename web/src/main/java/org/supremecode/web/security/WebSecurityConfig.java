package org.supremecode.web.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

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

    @Bean
    SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            JwtGrantedAuthoritiesConverter jwtAuthenticationConverter
    ) {
        return http
                .authorizeExchange(auth ->
                        auth
                                .pathMatchers("/api/public/**").permitAll()
                                .pathMatchers("/actuator/**").permitAll()
                                .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .csrf(it -> it.disable())
                .build();
    }
}
