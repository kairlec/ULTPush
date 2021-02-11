package com.kairlec.ultpush.bind

import com.google.inject.ConfigurationException
import com.google.inject.Guice
import com.google.inject.Key
import com.kairlec.ultpush.component.ULTComponentLifecycle
import org.slf4j.LoggerFactory

object ULTInject {
    private val guice = Guice.createInjector(ULTModule())

    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        ULTComponentLifecycle.run()
    }

    fun <T> getInstance(clazz: Class<T>): T {
        return guice.getInstance(clazz)
    }

    fun <T> getInstanceOrNull(clazz: Class<T>): T? {
        return try {
            guice.getInstance(clazz)
        } catch (e: ConfigurationException) {
            logger.error("Cannot get instance for class '${clazz.name}' because ${e.message}", e)
            null
        }
    }

    fun <T> getGenericInstance(key: Key<T>, hard: Boolean): T {
        return guice.getGenericInstance(key, hard)
    }

    fun <T> getGenericInstanceOrNull(key: Key<T>, hard: Boolean): T? {
        return try {
            guice.getGenericInstance(key, hard)
        } catch (e: TypeCastException) {
            null
        } catch (e: ConfigurationException) {
            logger.error("Cannot get instance for key '${key.typeLiteral.type.typeName}' because ${e.message}", e)
            null
        } catch (e: IllegalArgumentException) {
            logger.error("Cannot get instance for key '${key.typeLiteral.type.typeName}' because ${e.message}", e)
            null
        }
    }

    fun <T> getGenericInstances(key: Key<T>, hard: Boolean): List<T> {
        return guice.getGenericInstances(key, hard)
    }

}

