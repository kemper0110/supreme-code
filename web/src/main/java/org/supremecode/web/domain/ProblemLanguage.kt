package org.supremecode.web.domain

import jakarta.persistence.*

@Entity
@Table(name = "problem_language")
open class ProblemLanguage() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    open lateinit var problem: Problem

    open lateinit var languageId: String

    @OneToMany(mappedBy = "problemLanguage", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    open var solutions: MutableList<Solution> = ArrayList()

    constructor(languageId: String, problem: Problem) : this() {
        this.languageId = languageId
        this.problem = problem
    }
}
