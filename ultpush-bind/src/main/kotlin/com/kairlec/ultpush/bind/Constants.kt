package com.kairlec.ultpush.bind

import com.kairlec.ultpush.plugin.ULTPluginImplLifecycle
import kotlinx.coroutines.delay

suspend fun runLifecycle() {
    //这里显式调用guice,使其初始化,防止注册未运行
    @Suppress("SENSELESS_COMPARISON")
    if (injector == null) {
        delay(10)
    }
    ULTPluginImplLifecycle.run()
}

suspend fun stop() {
    ULTPluginImplLifecycle.destroy()
}