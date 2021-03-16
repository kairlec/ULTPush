package com.kairlec.ultpush.component.lifecycle

import kotlin.reflect.KFunction

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ULTLoad

data class LoadCycle(
    override val annotation: ULTLoad,
    override val function: KFunction<*>
) : Cycle<ULTLoad>