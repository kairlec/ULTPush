@file:Suppress("UNCHECKED_CAST")

package com.kairlec.ultpush.bind

import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.TypeLiteral
import com.kairlec.ultpush.component.LifecycleException
import com.kairlec.ultpush.getULTPluginImpl
import com.kairlec.ultpush.plugin.ULTPluginImpl
import kotlinx.coroutines.channels.Channel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.NullPointerException
import kotlin.reflect.full.companionObjectInstance

internal val injector: Injector = Guice.createInjector(ULTInternalModule())


/**
 * 这个是公开的Injector,调用的时候必须经过依赖检查
 */
object ULTInjector {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private suspend fun <T : Any> check(
        clazz: Class<T>,
        name: String,
        eventNotFound: () -> T,
        eventOK: ULTPluginImpl.(Any) -> T,
        eventFailed: ULTPluginImpl.(Any) -> T
    ): T {
        val pluginImpl = getULTPluginImpl[clazz] ?: getULTPluginImpl[name] ?: return eventNotFound()
        val channel: Channel<Boolean> = Channel(1)
        pluginImpl.ifStatus(ULTPluginImpl.ULTPluginImplStatus.RUNNING, {
            logger.info("[$name][${clazz.name}]checked running")
            channel.send(true)
        }) {
            when (it) {
                ULTPluginImpl.ULTPluginImplStatus.FAILED -> {
                    logger.info("[$name][${clazz.name}]waited failed")
                    channel.send(false)
                }
                ULTPluginImpl.ULTPluginImplStatus.RUNNING -> {
                    logger.info("[$name][${clazz.name}]waited running")
                    channel.send(true)
                }
                else -> {
                }
            }
        }
        val ok = channel.receive()
        return if (ok) {
            eventOK(pluginImpl, pluginImpl.instance)
        } else {
            eventFailed(pluginImpl, pluginImpl.instance)
        }
    }

    private suspend fun <T : Any> checkNullable(
        clazz: Class<T>,
        name: String,
        eventNotFound: () -> T?,
        eventOK: ULTPluginImpl.(Any) -> T?,
        eventFailed: ULTPluginImpl.(Any) -> T?
    ): T? {
        val pluginImpl = getULTPluginImpl[clazz] ?: getULTPluginImpl[name] ?: return eventNotFound()
        val channel: Channel<Boolean> = Channel(1)
        pluginImpl.ifStatus(ULTPluginImpl.ULTPluginImplStatus.RUNNING, {
            channel.send(true)
        }) {
            if (it == ULTPluginImpl.ULTPluginImplStatus.RUNNING) {
                channel.send(true)
            } else {
                channel.send(false)
            }
        }
        val ok = channel.receive()
        return if (ok) {
            eventOK(pluginImpl, pluginImpl.instance)
        } else {
            eventFailed(pluginImpl, pluginImpl.instance)
        }
    }

    suspend fun <T : Any> getInstance(clazz: Class<T>): T {
        return check(clazz, clazz.name, { ULTInternalInjector.getInstance(clazz) }, { it as T }) {
            throw LifecycleException(
                this.clazz.kotlin,
                name,
                null,
                "get instance failed because an error occurred while completing the life cycle"
            )
        }
    }

    suspend fun <T : Any> getInstanceOrNull(clazz: Class<T>): T? {
        return checkNullable(clazz, clazz.name, { ULTInternalInjector.getInstanceOrNull(clazz) }, { it as T? }) {
            throw LifecycleException(
                this.clazz.kotlin,
                name,
                null,
                "get instance failed because an error occurred while completing the life cycle"
            )
        }
    }

    suspend fun <T : Any> getGenericInstance(clazz: Class<T>, assignable: Boolean): T {
        return check(clazz, clazz.name, { ULTInternalInjector.getGenericInstance(clazz, assignable) }, { it as T }) {
            throw LifecycleException(
                this.clazz.kotlin,
                name,
                null,
                "get instance failed because an error occurred while completing the life cycle"
            )
        }
    }

    suspend fun <T : Any> getGenericInstanceOrNull(clazz: Class<T>, assignable: Boolean): T? {
        return checkNullable(
            clazz,
            clazz.name,
            { ULTInternalInjector.getGenericInstanceOrNull(clazz, assignable) },
            { it as T? }) {
            throw LifecycleException(
                this.clazz.kotlin,
                name,
                null,
                "get instance failed because an error occurred while completing the life cycle"
            )
        }
    }


    suspend fun <T : Any> getGenericInstance(key: Key<T>, assignable: Boolean): T {
        val instance = ULTInternalInjector.getGenericInstance(key, assignable)
        return check(instance::class.java as Class<T>, key.typeLiteral.type.typeName, { instance }, { instance }) {
            throw LifecycleException(
                this.clazz.kotlin,
                name,
                null,
                "get instance failed because an error occurred while completing the life cycle"
            )
        }
    }

    suspend fun <T : Any> getGenericInstances(key: Key<T>, assignable: Boolean): List<T> {
        val instances = ULTInternalInjector.getGenericInstances(key, assignable)
        val validInstances = ArrayList<T>(instances.size)
        instances.forEach { instance ->
            val target = checkNullable(
                instance::class.java as Class<T>,
                key.typeLiteral.type.typeName,
                { instance },
                { instance }) {
                logger.warn(
                    "", LifecycleException(
                        this.clazz.kotlin,
                        name,
                        null,
                        "get instance failed because an error occurred while completing the life cycle"
                    )
                )
                null
            }
            if (target != null) {
                validInstances.add(target)
            }
        }
        return validInstances
    }
}


/**
 * 这个是用来加载到ULTLoad方法里的的注入器
 * 此注入器不进行任何的依赖检查,只进行注入
 */
internal object ULTInternalInjector {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun <T : Any> getInstance(clazz: Class<T>): T {
        clazz.kotlin.companionObjectInstance?.let {
            if (it is TypeLiteral<out Any>) {
                if (it.type != null) {
                    val target = getGenericInstanceOrNull(Key.get(it.type), false) as? T
                    if (target != null) {
                        return target
                    }
                }
            }
        }
        return injector.getInstance(clazz)
    }

    fun <T : Any> getInstanceOrNull(clazz: Class<T>): T? {
        return try {
            getInstance(clazz)
        } catch (e: Throwable) {
            logger.error("Cannot get instance for class '${clazz.name}' because ${e.message}", e)
            null
        }
    }

    fun <T : Any> getGenericInstance(clazz: Class<T>, assignable: Boolean): T {
        clazz.kotlin.companionObjectInstance?.let {
            if (it is TypeStrict) {
                if (it.type != null) {
                    return injector.getGenericInstance(Key.get(it.type), assignable) as T
                } else {
                    throw NullPointerException("${clazz.name} is TypeLiteralAble but typeLiteral is null")
                }
            } else {
                throw ClassCastException("${clazz.name} companion object instance is not TypeLiteralAble")
            }
        } ?: throw NullPointerException("${clazz.name} companion object is null")
    }

    fun <T : Any> getGenericInstanceOrNull(clazz: Class<T>, assignable: Boolean): T? {
        try {
            clazz.kotlin.companionObjectInstance?.let {
                if (it is TypeStrict) {
                    if (it.type != null) {
                        logger.info("type name=${it.type!!.type.typeName}")
                        logger.info("key=${Key.get(it.type)}")
                        return injector.getGenericInstance(Key.get(it.type), assignable) as? T?
                    }
                }
            }
            return null
        } catch (e: Throwable) {
            logger.error("getGenericInstance error:${e.message}", e)
            return null
        }
    }

    fun <T : Any> getGenericInstance(key: Key<T>, assignable: Boolean): T {
        return injector.getGenericInstance(key, assignable)
    }

    fun <T : Any> getGenericInstanceOrNull(key: Key<T>, assignable: Boolean): T? {
        return try {
            return getGenericInstance(key, assignable)
        } catch (e: Throwable) {
            logger.error("Cannot get instance for key '${key.typeLiteral.type.typeName}' because ${e.message}", e)
            null
        }
    }

    fun <T : Any> getGenericInstances(key: Key<T>, assignable: Boolean): List<T> {
        return injector.getGenericInstances(key, assignable)
    }
}
