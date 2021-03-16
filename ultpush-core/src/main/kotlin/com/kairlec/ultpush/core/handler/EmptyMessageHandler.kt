package com.kairlec.ultpush.core.handler

import com.kairlec.ultpush.core.pusher.PusherMsg
import com.kairlec.ultpush.core.receiver.ReceiverMsg

abstract class EmptyMessageHandler<T> : MessageHandler<T, T>() where T : PusherMsg, T : ReceiverMsg {
    override fun handle(receiverMsg: T): T {
        return receiverMsg
    }

    override val name = "[EmptyMessageHandler]unnamed@${hashCode()}"

    override fun allow(content: T) = true

    override fun toString(): String {
        return name
    }
}