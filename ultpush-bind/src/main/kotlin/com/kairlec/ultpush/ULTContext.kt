package com.kairlec.ultpush

import com.google.inject.TypeLiteral
import com.kairlec.ultpush.bind.ULTImpl
import com.kairlec.ultpush.bind.ULTInterface
import com.kairlec.ultpush.bind.ULTInternalInjector
import com.kairlec.ultpush.plugin.ULTPlugin
import com.kairlec.ultpush.plugin.ULTPluginImpl
import com.kairlec.ultpush.plugin.ULTPluginInterface
import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.KClass

class MultiNamespaceException(val namespace: String, override val message: String?) : Exception(message)
class MultiImplNameException(val name: String, override val message: String?) : Exception(message)
internal object ULTContextManager {

    private data class NamespaceNamePluginImpl(
        val namespace: String,
        val name: String,
        val pluginImpl: ULTPluginImpl
    )

    val plugins = HashMap<String, ULTPlugin>()
    val impls = HashMap<Class<*>, ULTPluginImpl>()
    val interfaces = HashMap<Class<*>, ULTPluginInterface>()
    private val implNamesWithNamespace = HashMap<String, LinkedList<NamespaceNamePluginImpl>>()

    fun getImpl(clazz: Class<out Any>): ULTPluginImpl? {
        return impls[clazz] ?: run {
            ULTInternalInjector.getInstanceOrNull(clazz)?.let {
                impls[it::class.java]
            }
        }
    }

    fun getImpl(clazz: KClass<out Any>): ULTPluginImpl? {
        return getImpl(clazz.java)
    }

    fun getImpl(namespace: String, name: String): ULTPluginImpl? {
        return implNamesWithNamespace[name]?.first { it.namespace == namespace }?.pluginImpl
    }

    fun getImpl(name: String): ULTPluginImpl? {
        val index = name.indexOf(':')
        return if (index == -1) {
            implNamesWithNamespace[name]?.singleOrNull()?.pluginImpl
        } else {
            getImpl(name.substring(0, index), name.substring(index + 1))
        }
    }

    fun getImpls(name: String): List<ULTPluginImpl> {
        return implNamesWithNamespace[name]?.map { it.pluginImpl } ?: emptyList()
    }

    fun submit(interfaceAnnotation: ULTInterface, clazz: Class<*>, plugin: ULTPlugin): ULTPluginInterface {
        return interfaces.computeIfAbsent(clazz) {
            ULTPluginInterface(interfaceAnnotation, clazz, plugin).apply {
                (plugin.registeredInterfaces as MutableList).add(this)
            }
        }
    }

    fun submit(
        implAnnotation: ULTImpl,
        clazz: Class<*>,
        plugin: ULTPlugin,
        tpl: TypeLiteral<out Any>?,
        name: String,
        order: Int,
        pluginInterface: ULTPluginInterface
    ): ULTPluginImpl {
        implNamesWithNamespace[name]?.let { lt ->
            if (lt.find { it.namespace == plugin.namespace } != null) {
                throw MultiImplNameException(name, "Multi impl name conflict:$name in namespace:${plugin.name}")
            }
        }
        return impls.computeIfAbsent(clazz) {
            ULTPluginImpl(implAnnotation, clazz, plugin, tpl, name, order).apply {
                (plugin.registeredImpls as MutableList).add(this)
            }
        }.apply {
            if (pluginInterface !in interfaces) {
                (interfaces as MutableList).add(pluginInterface)
            }
            if (this !in pluginInterface.impls) {
                (pluginInterface.impls as MutableList).add(this)
            }
        }.apply {
            implNamesWithNamespace[name]?.add(NamespaceNamePluginImpl(plugin.namespace, name, this)) ?: let {
                implNamesWithNamespace[name] = LinkedList<NamespaceNamePluginImpl>().apply {
                    add(NamespaceNamePluginImpl(plugin.namespace, name, it))
                }
            }
        }
    }

    fun submit(
        namespace: String,
        name: String,
        version: String? = null,
        versionCode: Int? = null,
        packages: MutableList<String>,
        classLoaders: List<ClassLoader>
    ): ULTPlugin {
        if (plugins[namespace] != null) {
            throw MultiNamespaceException(namespace, "Multi namespace conflict:$namespace")
        }
        return plugins.computeIfAbsent(namespace) {
            ULTPlugin(namespace, name, version ?: "Unknown", versionCode ?: 0, packages, classLoaders = classLoaders)
        }
    }
}

fun getULTPluginImpl(clazz: Class<out Any>): ULTPluginImpl? {
    return ULTContextManager.getImpl(clazz)
}

fun getULTPluginImpl(clazz: KClass<out Any>): ULTPluginImpl? {
    return ULTContextManager.getImpl(clazz)
}

fun getULTPluginImpl(name: String): ULTPluginImpl? {
    return ULTContextManager.getImpl(name)
}

fun getULTPluginImpl(namespace: String, name: String): ULTPluginImpl? {
    return ULTContextManager.getImpl(namespace, name)
}

fun getULTPluginImpls(name: String): List<ULTPluginImpl> {
    return ULTContextManager.getImpls(name)
}

object getULTPluginImpl {
    operator fun get(clazz: Class<out Any>): ULTPluginImpl? {
        return ULTContextManager.getImpl(clazz)
    }

    operator fun get(clazz: KClass<out Any>): ULTPluginImpl? {
        return ULTContextManager.getImpl(clazz)
    }

    operator fun get(name: String): ULTPluginImpl? {
        return ULTContextManager.getImpl(name)
    }

    operator fun get(namespace: String, name: String): ULTPluginImpl? {
        return ULTContextManager.getImpl(namespace, name)
    }

    fun gets(name: String): List<ULTPluginImpl> {
        return ULTContextManager.getImpls(name)
    }
}

open class ULTContext(
    val currentPlugin: ULTPlugin,
) {
    val allPlugins: Iterable<ULTPlugin> get() = ULTContextManager.plugins.values
}