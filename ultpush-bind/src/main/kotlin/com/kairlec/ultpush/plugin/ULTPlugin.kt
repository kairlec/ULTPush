package com.kairlec.ultpush.plugin

import java.util.*

class ULTPlugin(
    val namespace: String,
    val name: String,
    val version: String,
    val versionCode: Int,
    val packages: List<String>,
    val registeredInterfaces: List<ULTPluginInterface> = LinkedList(),
    val registeredImpls: List<ULTPluginImpl> = LinkedList(),
    val classLoaders: List<ClassLoader> = LinkedList()
) {
    override fun hashCode(): Int {
        return namespace.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return namespace == other
    }

    override fun toString(): String {
        return "ULTPlugin(namespace='$namespace', name='$name', version='$version', versionCode=$versionCode, packages=$packages, registeredInterfaces=$registeredInterfaces, registeredImpls=$registeredImpls, classLoaders=${classLoaders.map { "[${it.name}]${it}" }})"
    }
}

