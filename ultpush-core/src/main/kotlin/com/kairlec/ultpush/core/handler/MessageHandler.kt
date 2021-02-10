package com.kairlec.ultpush.core.handler

import com.kairlec.ultpush.core.Filter
import com.kairlec.ultpush.core.pusher.PusherMsg
import com.kairlec.ultpush.core.receiver.ReceiverMsg

/**
 * 消息处理器
 */
abstract class MessageHandler : Filter<ReceiverMsg> {
    open val name = "[MessageHandler]unnamed@${hashCode()}"
    abstract fun handle(receiverMsg: ReceiverMsg): PusherMsg

    override fun allow(content: ReceiverMsg) = true

    override fun toString(): String {
        return name
    }
}