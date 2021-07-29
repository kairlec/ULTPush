package com.kairlec.ultpush.configuration

import com.kairlec.ultpush.bind.ULTImpl

@ULTImpl("JacksonConfiguration")
class JacksonConfiguration : Configuration {

    override fun <T> load(name: String, clazz: Class<T>): T? {
        return Loader.load(Loader.Yaml.mapper, name, clazz, null, *Loader.Yaml.suffix)
            ?: Loader.load(Loader.Json.mapper, name, clazz, null, *Loader.Json.suffix)
            ?: Loader.load(Loader.Properties.mapper, name, clazz, null, *Loader.Properties.suffix)
    }

    override fun <T> load(name: String, clazz: Class<T>, event: T.() -> Unit) {
        load(name, clazz)?.apply(event)
    }

    override fun load(
        name: String,
        cached: Boolean,
        base: Config?
    ): Config? {
        return Loader.load(Loader.Yaml.mapper, name, cached, base, *Loader.Yaml.suffix)
            ?: Loader.load(Loader.Json.mapper, name, cached, base, *Loader.Json.suffix)
            ?: Loader.load(Loader.Properties.mapper, name, cached, base, *Loader.Properties.suffix)
    }

    override fun load(
        name: String,
        cached: Boolean,
        base: Config?,
        event: Config.() -> Unit
    ) {
        load(name, cached, base)?.apply(event)
    }

}