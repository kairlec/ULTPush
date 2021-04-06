package com.kairlec.ultpush.bind

import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.TypeLiteral
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable

val TypeLiteral<*>.wrapper get() = TypeLiteralWrapper(this)

@Suppress("UNCHECKED_CAST")
fun <T> Injector.getGenericInstance(key: Key<T>, assignable: Boolean): T {
    return bindings.keys.single { it.typeLiteral.wrapper.castAble(key.typeLiteral.wrapper, assignable) }
        ?.let { getInstance(it) as T }
        ?: throw TypeCastException(key.typeLiteral.type.typeName)
}

@Suppress("UNCHECKED_CAST")
fun <T> Injector.getGenericInstances(key: Key<T>, assignable: Boolean): List<T> {
    return bindings.keys.filter { it.typeLiteral.wrapper.castAble(key.typeLiteral.wrapper, assignable) }
        .map { getInstance(it) as T }
}

class TypeLiteralWrapperException(
    val type: Type,
    override val message: String,
    override val cause: Throwable? = null
) : RuntimeException(message, cause)

class TypeLiteralWrapper private constructor(val type: Type) {
    constructor(type: TypeLiteral<*>) : this(type.type)

    private val clazz: Class<*>
    private val genericClass: List<TypeLiteralWrapper>

    fun castAble(other: TypeLiteralWrapper, assignable: Boolean): Boolean {
        val result = if (assignable) {
            other.clazz.isAssignableFrom(this.clazz)
        } else {
            this.clazz == other.genericClass
        }
        if (!result) {
            return false
        }
        for (i in genericClass.indices) {
            if (!genericClass[i].castAble(other.genericClass[i], assignable)) {
                println("${genericClass[i].type.typeName} cannot cast to ${other.genericClass[i].type.typeName}")
                return false
            }
        }
        return true
    }

    init {
        when (type) {
            is Class<*> -> {
                clazz = type
                genericClass = emptyList()
            }
            is TypeVariable<*> -> {
                throw TypeLiteralWrapperException(type, "Cannot access unknown type variable")
            }
            is GenericArrayType -> {
                clazz = Array::class.java
                genericClass = listOf(TypeLiteralWrapper(type.genericComponentType))
            }
            is ParameterizedType -> {
                clazz = type.rawType as Class<*>
                genericClass = type.actualTypeArguments.map { TypeLiteralWrapper(it) }
            }
            else -> {
                throw TypeLiteralWrapperException(type, "no support type")
            }
        }
    }
}