package com.kairlec.ultpush.bind

import com.google.inject.Guice
import com.kairlec.ultpush.component.ULTComponentLifecycle

object ULTInject {
    private val guice = Guice.createInjector(ULTModule())

    init {
        ULTComponentLifecycle.run()
    }

    fun <T> getInstance(clazz: Class<T>): T {
        return guice.getInstance(clazz)
    }

}

