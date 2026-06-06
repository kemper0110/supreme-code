package org.supremecode.web.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.supremecode.web.domain.Tag
import org.supremecode.web.repository.TagRepository
import org.supremecode.web.views.TagView
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/tag")
class TagControllerImpl(
    private val tagRepository: TagRepository
) {

    @GetMapping
    @PreAuthorize("hasAuthority('tag:read')")
    fun getTags(): Mono<List<TagView>> {
        return Mono.just(tagRepository.findAll().map { tag -> TagView(tag.id!!, tag.name) })
    }

    @PostMapping
    @PreAuthorize("hasAuthority('tag:create') or hasAuthority('tag:update')")
    fun saveTag(@RequestBody tag: Tag): Mono<Void> {
        tagRepository.save(tag)
        return Mono.empty()
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('tag:delete')")
    fun deleteTag(@PathVariable id: Long): Mono<Void> {
        tagRepository.delete(tagRepository.getReferenceById(id))
        return Mono.empty()
    }
}
