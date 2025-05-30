package net.danil.web.domain

import jakarta.persistence.*

@Entity
@Table(name = "problem")
open class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    lateinit var author: User

    var name: String = ""

    var description: String = ""

    @ManyToMany(fetch = FetchType.LAZY)
    var tags: MutableList<Tag> = ArrayList()

    var difficulty: String = ""

    @OneToMany(mappedBy = "problem", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var languages: MutableList<ProblemLanguage> = ArrayList()

    @OneToMany(mappedBy = "problem", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var solutions: MutableList<Solution> = ArrayList()
}