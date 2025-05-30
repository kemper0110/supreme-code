package net.danil.web.domain

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import java.util.*

@Entity
@Table(name = "solution")
open class Solution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @CreatedDate
    @Column(nullable = false, updatable = false)
    lateinit var createdAt: Date

    @ManyToOne(fetch = FetchType.LAZY)
    lateinit var author: User

    @ManyToOne(fetch = FetchType.LAZY)
    lateinit var problemLanguage: ProblemLanguage

    var code: String = ""

    @OneToOne(mappedBy = "solution", cascade = [CascadeType.ALL])
    var solutionResult: SolutionResult? = null
}
