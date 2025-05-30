package net.danil.web.user.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import net.danil.web.domain.User;
import net.danil.web.repository.UserRepository;
import net.danil.web.user.security.TokenInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.Duration;
import java.util.*;

/**
 * SecurityService class
 *
 * @author Erik Amaru Ortiz
 */
@Component
@RequiredArgsConstructor
public class SecurityService implements Serializable {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private String defaultExpirationTimeInSecondsConf;

    @Value("${jwt.cookie-name}")
    private String cookieName;


    public TokenInfo generateAccessToken(User user) {
        var claims = new HashMap<String, Object>() {{
//            put("role", user.getRoles());
        }};

        return doGenerateToken(claims, user.getUsername(), user.getId().toString());
    }

    private TokenInfo doGenerateToken(Map<String, Object> claims, String issuer, String subject) {
        var expirationTimeInMilliseconds = Long.parseLong(defaultExpirationTimeInSecondsConf) * 1000;
        var expirationDate = new Date(new Date().getTime() + expirationTimeInMilliseconds);

        return doGenerateToken(expirationDate, claims, issuer, subject);
    }

    private TokenInfo doGenerateToken(Date expirationDate, Map<String, Object> claims, String issuer, String subject) {
        var createdDate = new Date();
        var token = Jwts.builder()
                .setClaims(claims)
                .setIssuer(issuer)
                .setSubject(subject)
                .setIssuedAt(createdDate)
                .setId(UUID.randomUUID().toString())
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, Base64.getEncoder().encodeToString(secret.getBytes()))
                .compact();

        return TokenInfo.builder()
                .token(token)
                .issuedAt(createdDate)
                .expiresAt(expirationDate)
                .build();
    }

    public String makeJwtCookie(TokenInfo tokenInfo) {
        return ResponseCookie.fromClientResponse(cookieName, tokenInfo.getToken())
                .maxAge(Duration.ofMillis(tokenInfo.getExpiresAt().getTime() - new Date().getTime()))
                .httpOnly(true)
                .path("/")
                .secure(false)
                .build().toString();
    }
}
