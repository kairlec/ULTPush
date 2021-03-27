package com.kairlec.ultpush.core.handler

import com.google.inject.TypeLiteral
import com.kairlec.ultpush.bind.ULTInterface
import com.kairlec.ultpush.core.Filter
import com.kairlec.ultpush.core.pusher.Pusher
import com.kairlec.ultpush.core.pusher.PusherMsg
import com.kairlec.ultpush.core.receiver.ReceiverMsg

/**
 * 消息处理器
 */
@ULTInterface(20)
abstract class MessageHandler<T : ReceiverMsg, R : PusherMsg> : Filter<T> {
    abstract fun getPusherTypeLiteral(): TypeLiteral<out Pusher<out PusherMsg>>

    open val name = "[MessageHandler]unnamed@${hashCode()}"

    abstract fun handle(receiverMsg: T): R

    override fun allow(content: T) = true

    override fun toString(): String {
        Thread.currentThread()
        return name
    }

}