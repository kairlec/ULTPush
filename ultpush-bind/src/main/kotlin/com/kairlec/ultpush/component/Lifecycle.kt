package com.kairlec.ultpush.component

import kotlin.reflect.KClass
import kotlin.reflect.KFunction

data class Lifecycle(
    val clazz: KClass<out Any>,
    val name: String,
    var instance: Any? = null,
    var loadFunction: KFunction<*>? = null,
    var loadAnn: ULTLoad? = null,
    var initFunction: KFunction<*>? = null,
    var initAnn: ULTInit? = null,
    var runFunction: KFunction<*>? = null,
    var runAnn: ULTRun? = null,
    var destroyFunction: KFunction<*>? = null,
    var destroyAnn: ULTDestroy? = null
) {
    override fun equals(other: Any?): Boolean = other is Lifecycle && other.clazz == clazz
    override fun hashCode(): Int {
        return clazz.hashCode()
    }
}