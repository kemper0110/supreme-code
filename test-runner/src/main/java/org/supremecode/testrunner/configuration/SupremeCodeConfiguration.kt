package org.supremecode.testrunner.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "supreme-code.test-runner")
data class TestRunnerProperties(
    val container: ContainerProperties
) {
    data class ContainerProperties(
        val ttk: Int = 12000
    )
}