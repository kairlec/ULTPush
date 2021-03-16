@file:Suppress("UNCHECKED_CAST")

package com.kairlec.ultpush.bind

import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Key
import com.kairlec.ultpush.component.LifecycleException
import com.kairlec.ultpush.component.ULTComponent
import com.kairlec.ultpush.component.ULTComponentLifecycle
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
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

    private fun <T : Any> check(
        clazz: Class<T>,
        name: String,
        eventNotFound: () -> T,
        eventOK: ULTComponent.(Any) -> T,
        eventFailed: ULTComponent.(Any) -> T
    ): T {
        val component = ULTComponentLifecycle[clazz] ?: ULTComponentLifecycle[name] ?: return eventNotFound()
        val channel: Channel<Boolean> = Channel(1)
        component.ifStatus(ULTComponent.ComponentStatus.RUNNING, {
            runBlocking { channel.send(true) }
        }) {
            runBlocking {
                when (it) {
                    ULTComponent.ComponentStatus.FAILED ->
                        channel.send(false)
                    ULTComponent.ComponentStatus.RUNNING ->
                        channel.send(true)
                    else -> {
                    }
                }
            }
        }
        return runBlocking {
            val ok = channel.receive()
            if (ok) {
                eventOK(component, component.instance)
            } else {
                eventFailed(component, component.instance)
            }
        }
    }

    private fun <T : Any> checkNullable(
        clazz: Class<T>,
        name: String,
        eventNotFound: () -> T?,
        eventOK: ULTComponent.(Any) -> T?,
        eventFailed: ULTComponent.(Any) -> T?
    ): T? {
        val component = ULTComponentLifecycle[clazz] ?: ULTComponentLifecycle[name] ?: return eventNotFound()
        val channel: Channel<Boolean> = Channel(1)
        component.ifStatus(ULTComponent.ComponentStatus.RUNNING, {
            runBlocking { channel.send(true) }
        }) {
            runBlocking {
                if (it == ULTComponent.ComponentStatus.RUNNING) {
                    channel.send(true)
                } else {
                    channel.send(false)
                }
            }
        }
        return runBlocking {
            val ok = channel.receive()
            if (ok) {
                eventOK(component, component.instance)
            } else {
                eventFailed(component, component.instance)
            }
        }
    }

    fun <T : Any> getInstance(clazz: Class<T>): T {
        return check(clazz, clazz.name, { ULTInternalInjector.getInstance(clazz) }, { it as T }) {
            throw LifecycleException(
                this.implClazz.kotlin,
                name,
                null,
                "get instance failed because an error occurred while completing the life cycle"
            )
        }
    }

    fun <T : Any> getInstanceOrNull(clazz: Class<T>): T? {
        return checkNullable(clazz, clazz.name, { ULTInternalInjector.getInstanceOrNull(clazz) }, { it as T? }) {
            throw LifecycleException(
                this.implClazz.kotlin,
                name,
                null,
                "get instance failed because an error occurred while completing the life cycle"
            )
        }
    }

    fun <T : Any> getGenericInstance(clazz: Class<T>, assignable: Boolean): T {
        return check(clazz, clazz.name, { ULTInternalInjector.getGenericInstance(clazz, assignable) }, { it as T }) {
            throw LifecycleException(
                this.implClazz.kotlin,
                name,
                null,
                "get instance failed because an error occurred while completing the life cycle"
            )
        }
    }

    fun <T : Any> getGenericInstanceOrNull(clazz: Class<T>, assignable: Boolean): T? {
        return checkNullable(
            clazz,
            clazz.name,
            { ULTInternalInjector.getGenericInstanceOrNull(clazz, assignable) },
            { it as T? }) {
            throw LifecycleException(
                this.implClazz.kotlin,
                name,
                null,
                "get instance failed because an error occurred while completing the life cycle"
            )
        }
    }


    fun <T : Any> getGenericInstance(key: Key<T>, assignable: Boolean): T {
        val instance = ULTInternalInjector.getGenericInstance(key, assignable)
        return check(instance::class.java as Class<T>, key.typeLiteral.type.typeName, { instance }, { instance }) {
            throw LifecycleException(
                this.implClazz.kotlin,
                name,
                null,
                "get instance failed because an error occurred while completing the life cycle"
            )
        }
    }

    fun <T : Any> getGenericInstances(key: Key<T>, assignable: Boolean): List<T> {
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
                        this.implClazz.kotlin,
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
            if (it is TypeLiteralAble) {
                if (it.typeLiteral != null) {
                    val target = getGenericInstanceOrNull(Key.get(it.typeLiteral), false) as? T
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
            if (it is TypeLiteralAble) {
                if (it.typeLiteral != null) {
                    return injector.getGenericInstance(Key.get(it.typeLiteral), assignable) as T
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
                if (it is TypeLiteralAble) {
                    if (it.typeLiteral != null) {
                        logger.info("type name=${it.typeLiteral!!.type.typeName}")
                        logger.info("key=${Key.get(it.typeLiteral)}")
                        return injector.getGenericInstance(Key.get(it.typeLiteral), assignable) as? T?
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
