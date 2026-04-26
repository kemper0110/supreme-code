package org.supremecode.web.domain

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "problem_tags")
open class ProblemTag() {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    open lateinit var problem: Problem

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    open lateinit var tag: Tag

    constructor(problem: Problem, tag: Tag) : this() {
        this.problem = problem
        this.tag = tag
    }
}