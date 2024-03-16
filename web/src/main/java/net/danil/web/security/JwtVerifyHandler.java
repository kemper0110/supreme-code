package net.danil.web.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.Date;

/**
 * JwtVerifyHandler class
 *
 * @author Erik Amaru Ortiz
 */
public class JwtVerifyHandler {
    private final String secret;

    public JwtVerifyHandler(String secret) {
        this.secret = secret;
    }

    public Mono<VerificationResult> check(String accessToken) {
        return Mono.just(verify(accessToken))
                .onErrorResume(e -> Mono.error(new RuntimeException(e.getMessage())));
    }

    private VerificationResult verify(String token) {
        var claims = getAllClaimsFromToken(token);
        final Date expiration = claims.getExpiration();

        if (expiration.before(new Date()))
            throw new RuntimeException("Token expired");

        return new VerificationResult(claims, token);
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(Base64.getEncoder().encodeToString(secret.getBytes()))
                .parseClaimsJws(token)
                .getBody();
    }

    @AllArgsConstructor
    public class VerificationResult {
        public Claims claims;
        public String token;
    }
}
