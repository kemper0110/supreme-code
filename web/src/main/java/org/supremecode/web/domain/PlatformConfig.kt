package org.supremecode.web.domain

data class LanguageConfig(
    val name: String,
    val iconPath: String,
    val monacoLanguageId: String,
    val fileExtension: String,
    val ephemeralFileName: String,
)

data class PlatformConfig(
    val languages: Map<String, LanguageConfig>
)