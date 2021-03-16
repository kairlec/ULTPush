package com.kairlec.ultpush.configuration

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.kairlec.ultpush.bind.ULTImpl
import java.io.File
import java.util.concurrent.ConcurrentHashMap

@ULTImpl("JacksonConfiguration")
class JacksonConfiguration : Configuration {
    companion object {
        val mapper = ObjectMapper(YAMLFactory())
        private val configMap = ConcurrentHashMap<String, Config>()
    }

    private fun <T> loadYaml(
        text: String?,
        clazz: Class<T>,
        objectMapper: ObjectMapper,
        objectReader: ObjectReader?
    ): T? {
        if (text == null) {
            return null
        }
        return if (objectReader != null) {
            objectReader.readValue(text, clazz)
        } else {
            objectMapper.readValue(text, clazz)
        }
    }

    private fun loadYaml(
        text: String?,
        objectMapper: ObjectMapper,
        objectReader: ObjectReader?
    ): JsonNode? {
        if (text == null) {
            return null
        }
        return if (objectReader != null) {
            objectReader.readTree(text)
        } else {
            objectMapper.readTree(text)
        }
    }

    override fun <T> loadYaml(name: String, clazz: Class<T>, base: T?, event: T.() -> Unit): T? {
        var reader: ObjectReader? = base?.let { mapper.readerForUpdating(it) }
        var obj = loadYaml(javaClass.getResourceAsStream("/$name.yml").text, clazz, mapper, reader)
        if (obj != null && reader == null) {
            reader = mapper.readerForUpdating(obj)
        }
        obj = loadYaml(File("./$name.yml").text, clazz, mapper, reader)
        if (obj != null && reader == null) {
            reader = mapper.readerForUpdating(obj)
        }
        obj = loadYaml(File("./config/$name.yml").text, clazz, mapper, reader)
        return obj?.apply(event)
    }

    override fun loadYaml(
        name: String,
        subName: String?,
        cached: Boolean,
        base: Config?,
        event: Config.() -> Unit
    ): Config? {
        if (base !is JacksonAsConfig?) {
            throw TypeCastException("Cannot cast non-JacksonAsConfig to JacksonAsConfig")
        }
        val config = if (cached) {
            configMap[name] ?: let {
                loadYaml(name, base?.node)?.let { JacksonAsConfig(it) }?.apply {
                    configMap[name] = this
                }
            }
        } else {
            loadYaml(name, base?.node)?.let { JacksonAsConfig(it) }?.apply {
                configMap[name] = this
            }
        }
        return subName?.let { config?.get(it)?.apply(event) } ?: config?.apply(event)
    }


    fun loadYaml(name: String, base: JsonNode?): JsonNode? {
        var reader: ObjectReader? = base?.let { mapper.readerForUpdating(it) }
        var node = loadYaml(javaClass.getResourceAsStream("/$name.yml").text, mapper, reader)
        if (node != null && reader == null) {
            reader = mapper.readerForUpdating(node)
        }
        node = loadYaml(File("./$name.yml").text, mapper, reader)
        if (node != null && reader == null) {
            reader = mapper.readerForUpdating(node)
        }
        node = loadYaml(File("./config/$name.yml").text, mapper, reader)
        return node
    }
}