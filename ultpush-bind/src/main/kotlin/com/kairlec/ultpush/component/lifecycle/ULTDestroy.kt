package com.kairlec.ultpush.component.lifecycle

import kotlin.reflect.KClass
import kotlin.reflect.KFunction

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ULTDestroy(val dependNames: Array<String> = [], val dependClasses: Array<KClass<out Any>> = [])

data class DestroyCycle(
    override val annotation: ULTDestroy,
    override val function: KFunction<*>
) : Cycle<ULTDestroy>
