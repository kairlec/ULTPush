package com.kairlec.ultpush.configuration

import java.lang.RuntimeException
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

class ConfigurationBinderException(
    override val message: String,
    val configurationClass: KClass<out Any>,
    override val cause: Throwable? = null
) : Exception(message, cause)

object ConfigurationBinder {
    fun <T : Any> solve(prefix: String, configurationClass: KClass<out T>) {
        val instance = solveConstructor(prefix, configurationClass)
        configurationClass.declaredMemberProperties
        configurationClass.memberProperties
    }

    private fun <T : Any> solveConstructor(prefix: String, configurationClass: KClass<out T>): T {
        configurationClass.primaryConstructor?.let {
            if (it.typeParameters.isEmpty()) {
                return it.call()
            }
        }
        configurationClass.constructors.forEach {
            if (it.typeParameters.isEmpty()) {
                return it.call()
            }
        }
        throw ConfigurationBinderException(
            "None empty argument constructor can be call:${configurationClass.qualifiedName}",
            configurationClass
        )
    }
}

