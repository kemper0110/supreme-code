package org.supremecode.shared

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.FileReader

data class LanguageConfig(
    val name: String,
    val iconPath: String,
    val monacoLanguageId: String,
    val monacoFile: String,
    val extensions: List<String>,
    val ephemeralFileName: String,
    val playgroundInitialCode: String,
    val testerConfig: TesterConfig? = null,
    val runnerConfig: RunnerConfig? = null,
    val lspConfig: LspConfig? = null,
)

data class TesterConfig(
    val imageName: String,
    val testPath: String,
    val solutionPath: String,
    val reportPath: String,
    val verdictClassName: String,
    val cmd: List<String>? = null,
)

data class RunnerConfig(
    val image: String,
    val filePath: String,
    val cmd: List<String>? = null,
)

data class LspConfig(
    val image: String,
    val cmd: List<String>? = null,
)

data class PlatformConfig(
    val languages: Map<String, LanguageConfig>
)

fun readPlatformConfig(): PlatformConfig {
    val objectMapper = ObjectMapper(YAMLFactory())
        .registerKotlinModule()

    val content = FileReader("C:\\Users\\danil\\IdeaProjects\\supreme-code\\platform.yaml").readText()
    val config = objectMapper.readValue(content, PlatformConfig::class.java)

    return config
}
