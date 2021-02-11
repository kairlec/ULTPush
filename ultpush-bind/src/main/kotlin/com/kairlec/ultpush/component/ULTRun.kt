package com.kairlec.ultpush.component

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.valueParameters

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ULTRun(
    val dependNames: Array<String> = [],
    val dependClasses: Array<KClass<*>> = [],
    val async: Boolean = true,
    val asyncTimeout: Int = 30
) {
    companion object {
        fun register(lifecycle: Lifecycle, function: KFunction<*>) {
            function.findAnnotation<ULTRun>()?.let {
                if (function.valueParameters.isNotEmpty()) {
                    throw IllegalArgumentException("Destroy function can only use empty parameter list")
                }
                lifecycle.runFunction?.run {
                    throw UnsupportedOperationException("not support for multi destroy function yet[$name,${function.name}]")
                }
                lifecycle.runAnn = it
                lifecycle.runFunction = function
            }
        }
    }
}