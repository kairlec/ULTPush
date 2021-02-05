package com.kairlec.ultpush.core.handler

import com.kairlec.ultpush.core.pusher.PusherMsg
import com.kairlec.ultpush.core.receiver.ReceiverMsg

/**
 * 消息处理器
 */
abstract class MessageHandler {
    open val name = "[MessageHandler]unnamed@${hashCode()}"
    abstract fun handle(receiverMsg: ReceiverMsg): PusherMsg

    override fun toString(): String {
        return name
    }
}