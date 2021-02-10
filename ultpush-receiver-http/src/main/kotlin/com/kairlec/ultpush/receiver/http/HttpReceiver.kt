package com.kairlec.ultpush.receiver.http

import com.kairlec.ultpush.core.Application
import com.kairlec.ultpush.core.AuthenticateStatus
import com.kairlec.ultpush.core.receiver.Receiver
import com.kairlec.ultpush.core.receiver.ReceiverMsg
import org.springframework.boot.runApplication

/**
 * HTTP接收器实例
 */
class HttpReceiver : Receiver() {

    companion object {
        lateinit var instance: HttpReceiver

        private fun start(target: HttpReceiver) {
            /**
             * 启动一个SpringBoot应用作为HTTP接收器
             */
            if (!this::instance.isInitialized) {
                instance = target
                runApplication<CustomSpringbootApplication>(*Application.args)
            }
        }
    }

    override suspend fun run() {
        start(this)
    }

    override fun authenticate(body: ReceiverMsg): AuthenticateStatus<ReceiverMsg> {
        TODO("Not yet implemented")
    }
}