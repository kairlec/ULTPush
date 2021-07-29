package com.kairlec.ultpush.configuration

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object Loader {
    private val configMap = ConcurrentHashMap<String, Config>()

    private fun tryLoadByClasspath(name: String, vararg suffix: String): String? {
        suffix.forEach {
            val text = JacksonConfiguration::class.java.classLoader.getResourceAsStream("$name.$it").text
                ?: Thread.currentThread().contextClassLoader.getResourceAsStream("$name.$it").text
            if (text != null) {
                return text
            }
        }
        return null
    }

    private fun tryLoadByPath(name: String, vararg suffix: String): String? {
        suffix.forEach {
            val text = File("./$name.$it").text
            if (text != null) {
                return text
            }
        }
        return null
    }

    private fun tryLoadByConfigPath(name: String, vararg suffix: String): String? {
        suffix.forEach {
            val text = File("./config/$name.$it").text
            if (text != null) {
                return text
            }
        }
        return null
    }

    private fun tryLoadUpdate(
        mapper: ObjectMapper,
        name: String,
        base: JsonNode?,
        vararg suffix: String
    ): JsonNode? {
        var reader: ObjectReader? = base?.let { mapper.readerForUpdating(it) }
        var node = tryLoadUpdate(
            mapper,
            tryLoadByClasspath(name, *suffix),
            reader
        )
        if (node != null && reader == null) {
            reader = mapper.readerForUpdating(node)
        }
        node = tryLoadUpdate(mapper, tryLoadByPath(name, *suffix), reader)
        if (node != null && reader == null) {
            reader = mapper.readerForUpdating(node)
        }
        node = tryLoadUpdate(mapper, tryLoadByConfigPath(name, *suffix), reader)
        return node
    }

    private fun <T> tryLoadUpdate(
        mapper: ObjectMapper,
        text: String?,
        clazz: Class<T>,
        objectReader: ObjectReader?
    ): T? {
        if (text == null) {
            return null
        }
        return if (objectReader != null) {
            objectReader.readValue(text, clazz)
        } else {
            mapper.readValue(text, clazz)
        }
    }

    private fun tryLoadUpdate(
        mapper: ObjectMapper,
        text: String?,
        objectReader: ObjectReader?
    ): JsonNode? {
        if (text == null) {
            return null
        }
        return if (objectReader != null) {
            objectReader.readTree(text)
        } else {
            mapper.readTree(text)
        }
    }

    fun <T> load(mapper: ObjectMapper, name: String, clazz: Class<T>, base: T?, vararg suffix: String): T? {
        var reader: ObjectReader? = base?.let { mapper.readerForUpdating(it) }
        var node = tryLoadUpdate(mapper, tryLoadByClasspath(name, *suffix), clazz, reader)
        if (node != null && reader == null) {
            reader = mapper.readerForUpdating(node)
        }
        node = tryLoadUpdate(mapper, tryLoadByPath(name, *suffix), clazz, reader)
        if (node != null && reader == null) {
            reader = mapper.readerForUpdating(node)
        }
        node = tryLoadUpdate(mapper, tryLoadByConfigPath(name, *suffix), clazz, reader)
        return node
    }

    fun load(
        mapper: ObjectMapper,
        name: String,
        cached: Boolean,
        base: Config?,
        vararg suffix: String
    ): Config? {
        if (base !is JacksonAsConfig?) {
            throw TypeCastException("Cannot cast non-JacksonAsConfig to JacksonAsConfig")
        }
        return if (cached) {
            configMap[name] ?: let {
                tryLoadUpdate(mapper, name, base?.node, *suffix)?.let { JacksonConfigFactory.create(it, mapper) }?.apply {
                    configMap[name] = this
                }
            }
        } else {
            tryLoadUpdate(mapper, name, base?.node, *suffix)?.let { JacksonConfigFactory.create(it, mapper) }?.apply {
                configMap[name] = this
            }
        }
    }

    object Yaml {
        val mapper = ObjectMapper(YAMLFactory())
        val suffix = arrayOf("yml", "yaml")
    }

    object Json {
        val mapper = ObjectMapper()
        val suffix = arrayOf("json")
    }

    object Properties {
        val mapper: JavaPropsMapper = JavaPropsMapper()
        val suffix = arrayOf("properties")
    }

}