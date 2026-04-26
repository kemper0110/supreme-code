package org.supremecode.web.controller

import org.springframework.web.bind.annotation.*
import org.supremecode.web.domain.Tag
import org.supremecode.web.repository.TagRepository
import org.supremecode.web.views.TagView

interface TagController {

}

@RestController
@RequestMapping("/api/tag")
class TagControllerImpl(
    private val tagRepository: TagRepository
) : TagController {

    @GetMapping
    fun getTags(): List<TagView> {
        return tagRepository.findAll().map { tag -> TagView(tag.id!!, tag.name) }
    }

    @PostMapping
    fun saveTag(@RequestBody tag: Tag) {
        tagRepository.save(tag)
    }

    @DeleteMapping("/{id}")
    fun deleteTag(@PathVariable id: Long) {
        tagRepository.delete(tagRepository.getReferenceById(id))
    }
}
