package com.kairlec.ultpush.component.lifecycle

import kotlin.reflect.KClass
import kotlin.reflect.KFunction

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ULTRun(
    val dependNames: Array<String> = [],
    val dependClasses: Array<KClass<*>> = [],
    val async: Boolean = true
)

data class RunCycle(
    override val annotation: ULTRun,
    override val function: KFunction<*>
) : Cycle<ULTRun>