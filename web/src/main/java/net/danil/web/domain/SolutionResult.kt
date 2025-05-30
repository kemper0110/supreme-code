package net.danil.web.domain

import jakarta.persistence.*
import net.danil.web.repository.TestListJsonConverter
import java.util.*


@Table(name = "solution_result")
@Entity
open class SolutionResult {
    @Id
    var id: Long? = null

    @Column(updatable = false)
    lateinit var createdAt: Date

    var exitCode: Int = 0
    var stdout: String = ""
    var stderr: String = ""

    var time: Float = 0f
    var timedOut: Boolean = false

    var solved: Boolean = false

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    lateinit var solution: Solution

    @Convert(converter = TestListJsonConverter::class)
    @Column(columnDefinition = "text")
    val tests: MutableList<TestResult> = ArrayList()
}