package com.kairlec.ultpush.wework.pusher

import com.kairlec.ultpush.bind.ULTInjector
import com.kairlec.ultpush.core.Application
import com.kairlec.ultpush.getULTPluginImpl
import com.kairlec.ultpush.wework.message.*
import com.kairlec.ultpush.wework.toUser
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.*


class PusherTest {

    @Test
    fun testLoad(){
        println(Message.image)
    }

    @Test
    fun test() {
//        System.setProperty("proxySet", "true")
//        System.setProperty("proxyHost", "127.0.0.1")
//        System.setProperty("proxyPort", "8888")
        runBlocking {
            Application.start()
            println("started!")
            val pusher = ULTInjector.getGenericInstance(WeWorkPusher::class.java, true)
            println(pusher)
            println(pusher.name)
            println("pid=${Application.pid()}")
            Application.join()
        }
    }

}