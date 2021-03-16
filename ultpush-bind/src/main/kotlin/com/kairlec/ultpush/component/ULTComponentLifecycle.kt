package com.kairlec.ultpush.component

import com.google.inject.TypeLiteral
import com.kairlec.ultpush.bind.ULTInternalInjector
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.reflect.KClass

internal object ULTComponentLifecycle {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val components = TreeSet<ULTComponent>()

    fun register(clazz: Class<out Any>, tpl: TypeLiteral<out Any>?, name: String, order: Int) {
        logger.info("register component:${clazz.name}[${name}][order:${order}]")
        val component = ULTComponent(clazz, tpl, name, order)
        components.add(component)
        componentNameMap[name] = component
        componentClassMap[clazz] = component
    }

    internal val componentNameMap = HashMap<String, ULTComponent>()
    internal val componentClassMap = HashMap<Class<out Any>, ULTComponent>()

    operator fun get(clazz: Class<out Any>): ULTComponent? {
        return componentClassMap[clazz] ?: run {
            ULTInternalInjector.getInstanceOrNull(clazz)?.let {
                componentClassMap[it::class.java]
            }
        }
    }

    operator fun get(clazz: KClass<out Any>): ULTComponent? {
        return get(clazz.java)
    }

    operator fun get(name: String): ULTComponent? {
        return componentNameMap[name]
    }

    fun destroy() {
        components.forEach {
            val status = it.destroy()
            if (!status.status) {
                logger.warn("destroy component ${it.implClazz.name}(${it.name}) failed :${status}")
            }
        }
    }

    fun run() {
        try {
            components.forEach {
                it.load()
            }
            components.forEach {
                val status = it.init()
                if (!status.status) {
                    logger.error("init component ${it.implClazz.name}(${it.name}) failed:${status}")
                }
            }
            components.forEach {
                val status = it.run()
                if (!status.status) {
                    logger.error("run component ${it.implClazz.name}(${it.name}) failed:${status}")
                }
            }
            logger.info("run lifecycle finished")
        } catch (e: Throwable) {
            logger.error("run lifecycle failed", e)
        }
    }
}

fun getULTComponent(clazz: Class<out Any>): ULTComponent? {
    return ULTComponentLifecycle.componentClassMap[clazz] ?: run {
        ULTInternalInjector.getInstanceOrNull(clazz)?.let {
            ULTComponentLifecycle.componentClassMap[it::class.java]
        }
    }
}

fun getULTComponent(clazz: KClass<out Any>): ULTComponent? {
    return getULTComponent(clazz.java)
}

fun getULTComponent(name: String): ULTComponent? {
    return ULTComponentLifecycle.componentNameMap[name]
}
