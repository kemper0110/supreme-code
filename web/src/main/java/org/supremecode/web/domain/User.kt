package org.supremecode.web.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor
import lombok.Builder;

@Entity
@Table(name = "users")
open class User() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null

    open var username: String = ""

    open var email: String = ""

    open var avatar: String? = null

    open var password: String = ""

    @OneToMany(mappedBy = "author", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    open var solutions: MutableList<Solution> = ArrayList()

    @OneToMany(mappedBy = "author", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    open var problems: MutableList<Problem> = ArrayList()
}