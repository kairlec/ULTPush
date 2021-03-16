package com.kairlec.ultpush.configuration

import com.kairlec.ultpush.bind.ULTInterface

@ULTInterface(0)
interface Configuration {
    fun loadYaml(
        name: String,
        sub: String? = null,
        cached: Boolean = true,
        base: Config? = null,
        event: Config.() -> Unit = {}
    ): Config?

    fun <T> loadYaml(name: String, clazz: Class<T>, base: T? = null, event: T.() -> Unit = {}): T?
}