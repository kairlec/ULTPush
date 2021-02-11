package com.kairlec.ultpush.core.receiver

import com.google.inject.TypeLiteral
import com.kairlec.ultpush.bind.ULTInterface
import com.kairlec.ultpush.component.ULTInterfaceType
import com.kairlec.ultpush.core.Application
import com.kairlec.ultpush.core.Authenticate
import com.kairlec.ultpush.core.Filter
import com.kairlec.ultpush.core.handler.MessageHandler
import com.kairlec.ultpush.core.pusher.Pusher
import java.lang.IllegalArgumentException

/**
 * 定义push的消息接收器
 */
@ULTInterface
abstract class Receiver<T : ReceiverMsg> : Authenticate<T>, Filter<T> {
    /**
     * 接收器的名称
     */
    open val name: String = "[Receiver]unnamed@${hashCode()}"

    @ULTInterfaceType
    val type = object : TypeLiteral<Receiver<T>>() {}

    override fun allow(content: T) = true

    override fun toString(): String {
        return name
    }

}