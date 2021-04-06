package com.kairlec.ultpush.configuration

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeType

class JacksonAsConfig(internal val node: JsonNode, private val fromMapper: ObjectMapper) : Config {
    override fun getChild(name: String): Config? {
        return node[name]?.let { JacksonAsConfig(it, fromMapper) }
    }

    override fun getChild(index: Int): Config? {
        return node[index]?.let { JacksonAsConfig(it, fromMapper) }
    }

    override fun <T> get(index: Int, event: Config.() -> T): T? {
        return get(index)?.let(event)
    }

    override fun <T> get(name: String, event: Config.() -> T): T? {
        return get(name)?.let(event)
    }

    override val type: ConfigType
        get() = when (node.nodeType) {
            JsonNodeType.ARRAY -> ConfigType.ARRAY
            JsonNodeType.BOOLEAN -> ConfigType.BOOLEAN
            JsonNodeType.NULL -> ConfigType.NULL
            JsonNodeType.NUMBER -> {
                if (node.isFloat) {
                    ConfigType.FLOAT
                } else {
                    ConfigType.INTEGER
                }
            }
            JsonNodeType.OBJECT -> ConfigType.OBJECT
            JsonNodeType.STRING -> ConfigType.STRING
            JsonNodeType.BINARY -> ConfigType.BINARY
            JsonNodeType.POJO,
            JsonNodeType.MISSING,
            -> ConfigType.UNKNOWN
            else -> ConfigType.UNKNOWN
        }
    override val data: Any?
        get() = when (node.nodeType) {
            JsonNodeType.ARRAY -> arrayValue
            JsonNodeType.BOOLEAN -> booleanValue
            JsonNodeType.NULL -> null
            JsonNodeType.NUMBER -> {
                if (node.isFloat) {
                    floatValue
                } else {
                    integerValue
                }
            }
            JsonNodeType.OBJECT -> objectValue
            JsonNodeType.STRING -> stringValue
            JsonNodeType.BINARY -> binaryValue
            JsonNodeType.POJO,
            JsonNodeType.MISSING,
            -> null
            else -> null
        }

    override fun <T> getData(clazz: Class<T>): T {
        return fromMapper.convertValue(node, clazz)
    }

    override val arrayValue: Iterable<Config>?
        get() = if (node.isArray) {
            (node as ArrayNode).map { JacksonAsConfig(it, fromMapper) }
        } else {
            null
        }

    override val objectValue: Config?
        get() = if (node.isObject) {
            JacksonAsConfig(node, fromMapper)
        } else {
            null
        }
    override val stringValue: String?
        get() = if (node.isTextual) {
            node.textValue()
        } else {
            null
        }
    override val booleanValue: Boolean?
        get() = if (node.isBoolean) {
            node.booleanValue()
        } else {
            null
        }
    override val integerValue: Int?
        get() = if (node.isIntegralNumber) {
            node.intValue()
        } else {
            null
        }
    override val floatValue: Float?
        get() = if (node.isFloatingPointNumber) {
            node.floatValue()
        } else {
            null
        }
    override val binaryValue: ByteArray?
        get() = if (node.isBinary) {
            node.binaryValue()
        } else {
            null
        }

    override fun asString(): String {
        return node.asText() ?: node.toString()
    }

    override fun <T> asString(event: String.() -> T) {
        event(asString())
    }
}