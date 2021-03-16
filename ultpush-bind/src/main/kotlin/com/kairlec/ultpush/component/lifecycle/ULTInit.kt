package com.kairlec.ultpush.component.lifecycle

import kotlin.reflect.KClass
import kotlin.reflect.KFunction

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ULTInit(val dependNames: Array<String> = [], val dependClasses: Array<KClass<*>> = [])

data class InitCycle(
    override val annotation: ULTInit,
    override val function: KFunction<*>
) : Cycle<ULTInit>