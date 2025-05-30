package net.danil.web.repository

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import net.danil.web.domain.TestResult

@Converter
class TestListJsonConverter : AttributeConverter<List<TestResult?>?, String> {
    override fun convertToDatabaseColumn(attribute: List<TestResult?>?): String {
        return objectMapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String): List<TestResult?>? {
        return objectMapper.readValue(dbData, object : TypeReference<List<TestResult?>?>() {})
    }

    companion object {
        private val objectMapper = ObjectMapper()
    }
}