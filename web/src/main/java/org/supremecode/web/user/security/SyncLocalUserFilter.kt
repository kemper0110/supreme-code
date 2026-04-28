package org.supremecode.web.user.security

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
                val keycloakId = jwt.subject!! // sub
                val email = jwt.getClaimAsString("email")
                val username = jwt.getClaimAsString("preferred_username") ?: email

                // блокирующий JPA → запускаем на elastic
                Mono.fromCallable {
                    var user = userRepository.findByKeycloakId(keycloakId)
                    if (user == null) {
                        // создаём новую локальную запись
                        user = User(
                            username = username ?: "",
                            keycloakId = keycloakId
                        )
                        userRepository.save(user)
                    }
                    // сохраняем локальный ID в атрибуты токена
                    auth.details = user
                    auth
                }.subscribeOn(Schedulers.boundedElastic())
            }
            .then(chain.filter(exchange))
    }
}