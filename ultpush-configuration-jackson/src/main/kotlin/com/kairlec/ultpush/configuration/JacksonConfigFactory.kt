package com.kairlec.ultpush.configuration

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

object JacksonConfigFactory {
    fun create(node: JsonNode, mapper: ObjectMapper): Config {
        return if (node.isArray || node.isObject) {
            JacksonAsIterableConfig(node, mapper)
        } else {
            @Suppress("DEPRECATION_ERROR")
            JacksonAsConfig(node, mapper)
        }
    }
}