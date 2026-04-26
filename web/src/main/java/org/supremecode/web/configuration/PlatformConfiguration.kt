package org.supremecode.web.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.supremecode.web.domain.PlatformConfig

@Configuration
open class PlatformConfiguration {
    @Bean
    open fun platformConfig(): PlatformConfig {
        val objectMapper = ObjectMapper(YAMLFactory())
            .registerKotlinModule()

        val stream = PlatformConfiguration::javaClass.javaClass.classLoader.getResourceAsStream("platform.yaml")
        val config = objectMapper.readValue(stream, PlatformConfig::class.java)

        return config
    }
}
