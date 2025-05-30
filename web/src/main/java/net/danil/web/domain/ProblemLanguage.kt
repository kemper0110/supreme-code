package net.danil.web.domain

import jakarta.persistence.*

@Entity
@Table(name = "problem_language")
open class ProblemLanguage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    lateinit var problem: Problem

    @ManyToOne(fetch = FetchType.LAZY)
    lateinit var language: Language

    var initialSolution: String = ""

    var preloaded: String = ""

    var tests: String = ""
}
