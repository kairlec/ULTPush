package com.kairlec.ultpush.plugin

import com.kairlec.ultpush.bind.ULTInterface
import java.util.*

class ULTPluginInterface(
    val annotation: ULTInterface,
    val clazz: Class<*>,
    val plugin: ULTPlugin,
    val impls: List<ULTPluginImpl> = LinkedList()
) {
    override fun hashCode(): Int {
        return clazz.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return clazz == other
    }

    override fun toString(): String {
        return "ULTPluginInterface(annotation=$annotation, clazz='${clazz.name}', plugin='${plugin.name}', impls=${impls.map { it.clazz.name }})"
    }
}
