package org.supremecode.web.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import kotlin.collections.get

@Component
class JwtGrantedAuthoritiesConverter : Converter<Jwt, Mono<JwtAuthenticationToken>> {
    companion object {
        private const val BACKEND_CLIENT_ID = "backend"
    }

    override fun convert(jwt: Jwt): Mono<JwtAuthenticationToken> {
        val authorities = mutableListOf<GrantedAuthority>()

        val realmAccess = jwt.claims.getOrDefault("realm_access", emptyMap<String, Any>()) as Map<*, *>
        val realmRoles = (realmAccess["roles"] as? Collection<*>)?.map { it.toString() } ?: emptyList()

        realmRoles.forEach { role ->
            authorities.add(SimpleGrantedAuthority("ROLE_$role"))
        }

        val resourceAccess = jwt.claims.getOrDefault("resource_access", emptyMap<String, Any>()) as Map<*, *>

        resourceAccess.forEach { (clientId, clientAccess) ->
            val clientRoles = ((clientAccess as? Map<*, *>)?.get("roles") as? Collection<*>)
                ?.map { it.toString() } ?: emptyList()

            clientRoles.forEach { role ->
                authorities.add(SimpleGrantedAuthority("PRIVILEGE_${clientId}:${role}"))
                if (clientId == BACKEND_CLIENT_ID) {
                    authorities.add(SimpleGrantedAuthority(role))
                }
            }
        }

        val scope = jwt.claims.getOrDefault("scope", "") as String
        scope.split(" ").forEach { s ->
            if (s.isNotBlank()) {
                authorities.add(SimpleGrantedAuthority("SCOPE_$s"))
            }
        }

        return Mono.just(JwtAuthenticationToken(jwt, authorities))
    }
}
