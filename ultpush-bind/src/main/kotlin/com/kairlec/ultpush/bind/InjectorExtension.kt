package com.kairlec.ultpush.bind

import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.TypeLiteral

val TypeLiteral<*>.wrapper get() = TypeLiteralWrapper(this)

@Suppress("UNCHECKED_CAST")
fun <T> Injector.getGenericInstance(key: Key<T>, assignable: Boolean): T {
    return bindings.keys.single { it.typeLiteral.wrapper.canCastTo(key.typeLiteral.wrapper, assignable) }
        ?.let { getInstance(it) as T }
        ?: throw TypeCastException(key.typeLiteral.type.typeName)
}

@Suppress("UNCHECKED_CAST")
fun <T> Injector.getGenericInstances(key: Key<T>, assignable: Boolean): List<T> {
    return bindings.keys.filter { it.typeLiteral.wrapper.canCastTo(key.typeLiteral.wrapper, assignable) }
        .map { getInstance(it) as T }
}


class TypeLiteralWrapper private constructor(val rawStr: String) {
    constructor(type: TypeLiteral<*>) : this(type.type.typeName)

    val clazz: Class<*>
    val allowSub: Boolean
    val genericClass: List<TypeLiteralWrapper>

    fun canCastTo(other: TypeLiteralWrapper, assignable: Boolean): Boolean {
        if (genericClass.size != other.genericClass.size) {
            return false
        }
        for (i in genericClass.indices) {
            if (!genericClass[i].canCastTo(other.genericClass[i], assignable)) {
                return false
            }
        }
        return clazz == other.clazz || (other.clazz.isAssignableFrom(clazz) && (assignable || other.allowSub))
    }

    init {
        if (rawStr == "?") {
            clazz = Any::class.java
            allowSub = true
            genericClass = emptyList()
        } else {
            var result: MatchResult?
            result = extendsReg.matchEntire(rawStr)
            if (result != null) {
                clazz = Class.forName(result.groupValues[1])
                allowSub = true
                genericClass = emptyList()
            } else {
                result = superReg.matchEntire(rawStr)
                if (result != null) {
                    clazz = Class.forName(result.groupValues[1])
                    allowSub = false
                    genericClass = emptyList()
                } else {
                    result = genReg.matchEntire(rawStr)
                    if (result != null) {
                        clazz = Class.forName(result.groupValues[1])
                        allowSub = false
                        genericClass = result.groupValues[2].split(",").map { TypeLiteralWrapper(it.trim()) }
                    } else {
                        error("Parse '$rawStr' failed on Regex:'${genReg.pattern}'")
                    }
                }
            }
        }
    }

    companion object {
        val superReg = """^([^<]+)$""".toRegex()
        val genReg = """^([^<]+)<(.*)>$""".toRegex()
        val extendsReg = """^\? extends (.*)$""".toRegex()
    }
}