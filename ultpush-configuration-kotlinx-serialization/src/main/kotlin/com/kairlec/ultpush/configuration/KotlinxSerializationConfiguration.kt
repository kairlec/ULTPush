package com.kairlec.ultpush.configuration

import com.kairlec.ultpush.bind.ULTImpl

@ULTImpl("KotlinxSerializationConfiguration")
class KotlinxSerializationConfiguration:Configuration {
    override fun load(name: String, cached: Boolean, base: Config?): Config? {

        TODO("Not yet implemented")
    }

    override fun load(name: String, cached: Boolean, base: Config?, event: Config.() -> Unit) {
        TODO("Not yet implemented")
    }

    override fun <T> load(name: String, clazz: Class<T>): T? {
        TODO("Not yet implemented")
    }

    override fun <T> load(name: String, clazz: Class<T>, event: T.() -> Unit) {
        TODO("Not yet implemented")
    }

}