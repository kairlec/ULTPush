package com.kairlec.ultpush.bind

import com.kairlec.ultpush.component.ULTComponentLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

fun runLifecycle() {
    runBlocking {
        //这里显式调用guice,使其初始化,防止注册未运行
        @Suppress("SENSELESS_COMPARISON")
        if (injector == null) {
            delay(10)
        }
    }
    ULTComponentLifecycle.run()
}

fun stop() {
    ULTComponentLifecycle.destroy()
}