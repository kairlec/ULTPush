package com.kairlec.ultpush

import com.kairlec.ultpush.plugin.ULTPluginImpl
import com.kairlec.ultpush.plugin.ULTPluginImplStatusChain
import com.kairlec.ultpush.plugin.success

abstract class ULTAbstractDepend {

    @set:Deprecated("Context shouldn't be set explicitly", level = DeprecationLevel.ERROR)
    lateinit var context: ULTContext
        internal set

    @set:Deprecated("CurrentImpl shouldn't be set explicitly", level = DeprecationLevel.ERROR)
    lateinit var currentImpl: ULTPluginImpl
        internal set

    data class NamespaceName(
        val namespace: String,
        val name: String,
    )

    fun awaitDependClasses(vararg names: NamespaceName): ULTPluginImplStatusChain {
        names.forEach {
            val impl = currentImpl.getDependImpl(it.namespace, it.name)
            val status = currentImpl.solveDepend(impl)
            if (!status.status) {
                return status
            }
        }
        return currentImpl.success()
    }

    fun awaitDependClasses(vararg names: Pair<String, String>): ULTPluginImplStatusChain {
        names.forEach {
            val impl = currentImpl.getDependImpl(it.first, it.second)
            val status = currentImpl.solveDepend(impl)
            if (!status.status) {
                return status
            }
        }
        return currentImpl.success()
    }

    fun awaitDependClasses(vararg names: String): ULTPluginImplStatusChain {
        names.forEach {
            val impl = currentImpl.getDependImpl(it)
            val status = currentImpl.solveDepend(impl)
            if (!status.status) {
                return status
            }
        }
        return currentImpl.success()
    }

    fun awaitDependClasses(vararg classes: Class<*>): ULTPluginImplStatusChain {
        classes.forEach {
            val impl = currentImpl.getDependImpl(it.kotlin)
            val status = currentImpl.solveDepend(impl)
            if (!status.status) {
                return status
            }
        }
        return currentImpl.success()
    }

}

