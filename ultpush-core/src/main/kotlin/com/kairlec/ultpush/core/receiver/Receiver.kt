package com.kairlec.ultpush.core.receiver

import com.kairlec.ultpush.core.Application
import com.kairlec.ultpush.core.Authenticate
import com.kairlec.ultpush.core.Component
import com.kairlec.ultpush.core.handler.MessageHandler
import java.lang.IllegalArgumentException

/**
 * 定义push的消息接收器
 */
abstract class Receiver : Component, Authenticate<ReceiverMsg> {
    /**
     * 接收器的名称
     */
    open val name: String = "[Receiver]unnamed@${hashCode()}"

    override fun init() {}
    override fun load() {}
    override fun destroy() {}

    suspend fun receive(msg: ReceiverMsg) {
        Application.handlerContext.values.forEach {
            it.handle(msg)
        }
    }

    fun receive(msg: ReceiverMsg, vararg handlers: MessageHandler) {
        handlers.forEach {
            it.handle(msg)
        }
    }

    suspend fun receive(msg: ReceiverMsg, vararg handlerNames: String) {
        val application = Application.start()
        handlerNames.forEach {
            application.getMessageHandler(it)?.handle(msg)
                ?: throw IllegalArgumentException("wrong handler name '$it' because it not exist in message handler context")
        }
    }

    override fun toString(): String {
        return name
    }

}