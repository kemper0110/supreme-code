package org.supremecode.web.domain

import jakarta.persistence.*

@Entity
@Table(name = "tag")
open class Tag() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null

    open var name: String = ""

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tag")
    open var problemTags: MutableList<ProblemTag> = ArrayList()

    constructor(id: Long?, name: String) : this() {
        this.id = id
        this.name = name
    }
}