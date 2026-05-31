package org.supremecode.web.domain

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.supremecode.shared.TestCaseResult
import java.util.*


@Table(name = "solution_result")
@Entity
open class SolutionResult() {
    @Id
    open var id: Long? = null

    @Column(updatable = false)
    open lateinit var createdAt: Date

    open var exitCode: Int = 0

    open var solved: Boolean = false

    open var total: Int = 0

    open var failures: Int = 0

    open var errors: Int = 0

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "test_cases", columnDefinition = "jsonb", nullable = false)
    open var testCases: List<TestCaseResult> = emptyList()

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    open lateinit var solution: Solution

    constructor(
        solution: Solution,
        exitCode: Int,
        solved: Boolean,
        total: Int,
        failures: Int,
        errors: Int,
        testCases: List<TestCaseResult>
    ) : this() {
        this.solution = solution
        this.createdAt = Date()
        this.exitCode = exitCode
        this.solved = solved
        this.total = total
        this.failures = failures
        this.errors = errors
        this.testCases = testCases
    }
}
