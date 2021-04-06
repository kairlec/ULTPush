package com.kairlec.ultpush.configuration

import com.kairlec.ultpush.bind.ULTInterface

@ULTInterface(0)
interface Configuration {
    fun load(
        name: String,
        cached: Boolean = true,
        base: Config? = null
    ): Config?

    fun load(
        name: String,
        cached: Boolean = true,
        base: Config? = null,
        event: Config.() -> Unit
    )

    fun <T> load(name: String, clazz: Class<T>, base: T? = null, event: T.() -> Unit)

    fun <T> load(name: String, clazz: Class<T>, base: T? = null): T?
}