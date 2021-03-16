package com.kairlec.ultpush.component

import kotlin.reflect.KClass

open class LifecycleException(
    val clazz: KClass<*>,
    val name: String,
    override val cause: Throwable?,
    override val message: String
) : Exception(message, cause)

class MultiLifecycleFunctionException(
    val LifecycleName: String,
    clazz: KClass<*>,
    name: String,
    message: String,
    cause: Throwable? = null,
) : LifecycleException(clazz, name, cause, message)