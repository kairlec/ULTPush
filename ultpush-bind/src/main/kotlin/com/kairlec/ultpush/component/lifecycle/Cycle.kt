package com.kairlec.ultpush.component.lifecycle

import kotlin.reflect.KFunction

interface Cycle<T : Annotation> {
    val annotation: T
    val function: KFunction<*>
}