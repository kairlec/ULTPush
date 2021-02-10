package com.kairlec.ultpush.component

import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.valueParameters

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ULTInit(val dependNames: Array<String> = [], val dependClasses: Array<KClass<*>> = []) {
    companion object {
        fun register(lifecycle: Lifecycle, function: KFunction<*>) {
            function.findAnnotation<ULTInit>()?.let {
                if (function.valueParameters.isNotEmpty()) {
                    throw IllegalArgumentException("Init function can only use empty parameter list")
                }
                lifecycle.initFunction?.run {
                    throw UnsupportedOperationException("not support for multi init function yet[$name,${function.name}]")
                }
                lifecycle.initAnn = it
                lifecycle.initFunction = function
            }
        }
    }
}