package com.kairlec.ultpush.component

import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.valueParameters

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ULTDestroy(val dependNames: Array<String> = [], val dependClasses: Array<KClass<out Any>> = []) {
    companion object {
        fun register(lifecycle: Lifecycle, function: KFunction<*>) {
            function.findAnnotation<ULTDestroy>()?.let {
                if (function.valueParameters.isNotEmpty()) {
                    throw IllegalArgumentException("Destroy function can only use empty parameter list")
                }
                lifecycle.destroyFunction?.run {
                    throw UnsupportedOperationException("not support for multi destroy function yet[$name,${function.name}]")
                }
                lifecycle.destroyAnn = it
                lifecycle.destroyFunction = function
            }
        }
    }
}