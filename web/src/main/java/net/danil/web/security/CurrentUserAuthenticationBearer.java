package net.danil.web.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

/**
 * CurrentUserAuthenticationBearer class
 *
 * @author Erik Amaru Ortiz
 */
public class CurrentUserAuthenticationBearer {
    public static Mono<Authentication> create(JwtVerifyHandler.VerificationResult verificationResult) {
        var claims = verificationResult.claims;
        var subject = claims.getSubject();
        var principalId = 0L;

        try {
            principalId = Long.parseLong(subject);
        } catch (NumberFormatException ignore) { }

        if (principalId == 0)
            return Mono.empty(); // invalid value for any of jwt auth parts

        var principal = new UserInfo(principalId, claims.getIssuer());

        return Mono.justOrEmpty(new UsernamePasswordAuthenticationToken(principal, null, null));
    }

}
