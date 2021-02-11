package com.kairlec.ultpush.core.handler

import com.google.inject.TypeLiteral
import com.kairlec.ultpush.bind.ULTInterface
import com.kairlec.ultpush.component.ULTInterfaceType
import com.kairlec.ultpush.core.Filter
import com.kairlec.ultpush.core.pusher.PusherMsg
import com.kairlec.ultpush.core.receiver.ReceiverMsg

/**
 * 消息处理器
 */
@ULTInterface
abstract class MessageHandler<T : ReceiverMsg, R : PusherMsg> : Filter<T> {

    @ULTInterfaceType
    val type = object : TypeLiteral<MessageHandler<T, R>>() {}

    open val name = "[MessageHandler]unnamed@${hashCode()}"

    abstract fun handle(receiverMsg: T): R

    override fun allow(content: T) = true

    override fun toString(): String {
        return name
    }
}