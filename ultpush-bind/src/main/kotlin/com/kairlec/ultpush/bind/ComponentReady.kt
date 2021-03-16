package com.kairlec.ultpush.bind

import kotlin.reflect.KClass

class ComponentReady(
    val clazz: KClass<*>,
    val name: String,
    val ok: Boolean,
    val cause: Throwable?,
    val msg: String?
)