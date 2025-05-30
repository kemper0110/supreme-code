package net.danil.web.domain

import jakarta.persistence.*

@Entity
@Table(name = "language")
open class Language {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    var name: String = ""

    var image: String = ""

    var podManifest: String = ""

    @OneToMany(mappedBy = "language", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var problemLanguages: MutableList<ProblemLanguage> = ArrayList()
}
