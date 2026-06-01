package org.supremecode.web.security

import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import org.supremecode.web.domain.User
import org.supremecode.web.repository.UserRepository
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class SyncLocalUserFilter(
    private val userRepository: UserRepository
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return ReactiveSecurityContextHolder.getContext()
            .mapNotNull { it.authentication as? JwtAuthenticationToken }
            .flatMap { auth ->
                if (auth?.token == null) {
                    return@flatMap Mono.error(RuntimeException("no auth token"))
                }
                val jwt = auth.token
                // sub
                val keycloakId = jwt.subject ?: return@flatMap Mono.error(RuntimeException("no keycloak id"))
                val email = jwt.getClaimAsString("email")
                val username = jwt.getClaimAsString("preferred_username") ?: email

                Mono.fromCallable {
                    var user = userRepository.findByKeycloakId(keycloakId)
                    if (user == null) {
                        user = userRepository.findByUsername(username)
                        if (user == null) {
                            user = User(
                                username = username ?: "",
                                keycloakId = keycloakId
                            )
                        } else {
                            user.keycloakId = keycloakId
                        }
                        userRepository.save(user)
                    }
                    auth.details = user
                    auth
                }.subscribeOn(Schedulers.boundedElastic())
            }
            .then(chain.filter(exchange))
    }
}
