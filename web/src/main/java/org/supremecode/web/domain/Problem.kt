package org.supremecode.web.domain

import jakarta.persistence.*
import java.util.LinkedHashSet

@Entity
@Table(name = "problem")
open class Problem() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null

    open var name: String = ""

    open var description: String = ""

    open var difficulty: String = ""

    @ManyToOne(fetch = FetchType.LAZY)
    open lateinit var author: User

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "problem", cascade = [CascadeType.ALL], orphanRemoval = true)
    open var problemTags: MutableSet<ProblemTag> = LinkedHashSet()

    @OneToMany(mappedBy = "problem", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    open var languages: MutableSet<ProblemLanguage> = LinkedHashSet()

    constructor(
        id: Long?,
        name: String,
        description: String,
        difficulty: String,
        languages: MutableSet<ProblemLanguage>,
        problemTags: MutableSet<ProblemTag>,
    ) : this() {
        this.id = id
        this.name = name
        this.description = description
        this.difficulty = difficulty
        this.languages = languages
        this.problemTags = problemTags
    }
}
