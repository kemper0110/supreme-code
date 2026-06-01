package org.supremecode.web.controller

import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.codec.ServerSentEvent
import org.supremecode.web.domain.User
import org.supremecode.web.service.SolutionResultNotification
import org.supremecode.web.service.SolutionResultNotificationService
import reactor.core.publisher.Flux
import java.time.Duration

@RestController
@PreAuthorize("hasAuthority('solution:view')")
@RequestMapping("/api/solution-results")
class SolutionResultNotificationController(
    private val notificationService: SolutionResultNotificationService,
) {
    @GetMapping("/events", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun events(auth: Authentication): Flux<ServerSentEvent<SolutionResultNotification>> {
        val authUser = auth.details as User
        val keepAlive = Flux.interval(Duration.ofSeconds(25))
            .map {
                ServerSentEvent.builder<SolutionResultNotification>()
                    .comment("keep-alive")
                    .build()
            }

        val notifications = notificationService.listen(authUser.id!!)
            .map {
                ServerSentEvent.builder(it)
                    .event("solution-result")
                    .id(it.solutionId.toString())
                    .build()
            }

        return Flux.merge(notifications, keepAlive)
    }
}
