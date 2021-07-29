package com.kairlec.ultpush.configuration

import kotlinx.serialization.DeserializationStrategy
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object Loader {
    private val configMap = ConcurrentHashMap<String, Config>()
    private fun tryLoadByClasspath(name: String, vararg suffix: String): String? {
        suffix.forEach {
            val text = KotlinxSerializationConfiguration::class.java.classLoader.getResourceAsStream("$name.$it").text
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

    private fun Config.update(base: Config, update: Config) {
    }

    fun <T> load(
        deserializer: DeserializationStrategy<T>,
        name: String,
        vararg suffix: String
    ): T? {
        return tryLoadByClasspath(name, *suffix)?.let { data ->
            Json.decodeFromString(deserializer, data)
        }
    }

    sealed interface Decoder {
        fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, data: String): T
    }

    object Json : Decoder {
        override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, data: String): T {
            return kotlinx.serialization.json.Json.decodeFromString(deserializer, data)
        }
    }


}