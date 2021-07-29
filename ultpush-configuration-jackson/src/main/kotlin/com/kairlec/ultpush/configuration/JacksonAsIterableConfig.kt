package com.kairlec.ultpush.configuration

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

@Suppress("DEPRECATION_ERROR")
class JacksonAsIterableConfig(node: JsonNode, fromMapper: ObjectMapper) : IterableConfig, JacksonAsConfig(
    node,
    fromMapper
) {
    init {
        if (!node.isArray && !node.isObject) {
            throw RuntimeException("node type is not array or object")
        }
    }

    override fun iterator(): Iterator<Config> {
        return node.elements().asSequence().map { JacksonConfigFactory.create(it, fromMapper) }.iterator()
    }
}