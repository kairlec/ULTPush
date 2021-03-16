package com.kairlec.ultpush.wework.pusher

import com.kairlec.ultpush.bind.ULTInjector
import com.kairlec.ultpush.bind.runLifecycle
import com.kairlec.ultpush.component.getULTComponent
import com.kairlec.ultpush.core.Application
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class PusherTest {
    @Test
    fun test() {
//        System.setProperty("proxySet", "true")
//        System.setProperty("proxyHost", "127.0.0.1")
//        System.setProperty("proxyPort", "8888")
        runBlocking {
            val application = Application.start()
            val pusher = ULTInjector.getGenericInstance(WeWorkPusher::class.java, true)
            val pusher2 = ULTInjector.getGenericInstance(WeWorkPusher::class.java, true)
            println("pid=${application.pid()}")
            println("1--2->${pusher === pusher2}")
            println(getULTComponent("WeWorkPusher")?.status)
            println(pusher.name)
            application.join()
        }
    }

}