package org.supremecode.web.domain

data class LanguageConfig(
    val name: String,
    val iconPath: String,
    val monacoLanguageId: String,
    val monacoFile: String,
    val extensions: List<String>,
    val ephemeralFileName: String,
    val playgroundInitialCode: String,
)

data class PlatformConfig(
    val languages: Map<String, LanguageConfig>
)