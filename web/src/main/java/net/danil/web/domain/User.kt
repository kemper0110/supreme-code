package net.danil.web.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
open class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var username: String = ""

    var email: String = ""

    var avatar: String = ""

    @OneToMany(mappedBy = "author", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var solutions: MutableList<Solution> = ArrayList()

    @OneToMany(mappedBy = "author", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var problems: MutableList<Problem> = ArrayList()
}