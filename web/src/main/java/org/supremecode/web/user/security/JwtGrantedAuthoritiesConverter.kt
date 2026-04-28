package org.supremecode.web.user.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class JwtGrantedAuthoritiesConverter : Converter<Jwt, Mono<JwtAuthenticationToken>> {

    override fun convert(jwt: Jwt): Mono<JwtAuthenticationToken> {
        val authorities = mutableListOf<GrantedAuthority>()

        // 1. Извлекаем realm-роли
        val realmAccess = jwt.claims.getOrDefault("realm_access", emptyMap<String, Any>()) as Map<*, *>
        val realmRoles = (realmAccess["roles"] as? Collection<*>)?.map { it.toString() } ?: emptyList()

        realmRoles.forEach { role ->
            // ROLE_ префикс для realm-ролей
            authorities.add(SimpleGrantedAuthority("ROLE_$role"))
        }

        // 2. Извлекаем client-роли (привилегии) из "resource_access"
        val resourceAccess = jwt.claims.getOrDefault("resource_access", emptyMap<String, Any>()) as Map<*, *>

        resourceAccess.forEach { (clientId, clientAccess) ->
            val clientRoles = ((clientAccess as? Map<*, *>)?.get("roles") as? Collection<*>)
                ?.map { it.toString() } ?: emptyList()

            clientRoles.forEach { role ->
                // Для client-ролей используем префикс PRIVILEGE_ или SCOPE_
                authorities.add(SimpleGrantedAuthority("PRIVILEGE_${clientId}:${role}"))
            }
        }

        // 3. scope из токена (если нужны)
        val scope = jwt.claims.getOrDefault("scope", "") as String
        scope.split(" ").forEach { s ->
            if (s.isNotBlank()) {
                authorities.add(SimpleGrantedAuthority("SCOPE_$s"))
            }
        }

        return Mono.just(JwtAuthenticationToken(jwt, authorities))
    }
}