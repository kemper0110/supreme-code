package net.danil.web.domain

import jakarta.persistence.*

@Entity
@Table(name = "tag")
open class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var name: String = ""

    @ManyToMany(fetch = FetchType.LAZY)
    var problems: MutableList<Problem> = ArrayList()
}