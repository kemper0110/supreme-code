package org.supremecode.web.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import java.util.*

@Entity
@Table(name = "solution")
open class Solution() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null

    @CreatedDate
    @Column(nullable = false, updatable = false)
    open lateinit var createdAt: Date

    @ManyToOne(fetch = FetchType.LAZY)
    open lateinit var author: User

    @ManyToOne(fetch = FetchType.LAZY)
    open lateinit var problemLanguage: ProblemLanguage

    @OneToOne(mappedBy = "solution", cascade = [CascadeType.ALL])
    open var solutionResult: SolutionResult? = null

    constructor(author: User, problemLanguage: ProblemLanguage) : this() {
        this.author = author
        this.problemLanguage = problemLanguage
        this.createdAt = Date()
    }
}
