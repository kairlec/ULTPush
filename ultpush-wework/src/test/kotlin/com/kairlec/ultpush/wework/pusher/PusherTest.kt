package com.kairlec.ultpush.wework.pusher

import com.kairlec.ultpush.bind.ULTInjector
import com.kairlec.ultpush.core.Application
import com.kairlec.ultpush.getULTPluginImpl
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test


class PusherTest {
    @Test
    fun test() {
//        System.setProperty("proxySet", "true")
//        System.setProperty("proxyHost", "127.0.0.1")
//        System.setProperty("proxyPort", "8888")
        runBlocking {
            Application.start()
            val pusher = ULTInjector.getGenericInstance(WeWorkPusher::class.java, true)
            val pusher2 = ULTInjector.getGenericInstance(WeWorkPusher::class.java, true)
            println("pid=${Application.pid()}")
            println("1--2->${pusher === pusher2}")
            println(getULTPluginImpl("WeWorkPusher")?.status)
            println(pusher.name)

            Application.join()
        }
    }

}