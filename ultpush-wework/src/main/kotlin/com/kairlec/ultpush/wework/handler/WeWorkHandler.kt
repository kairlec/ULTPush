package com.kairlec.ultpush.wework.handler

import com.google.inject.TypeLiteral
import com.kairlec.ultpush.bind.TypeStrict
import com.kairlec.ultpush.bind.ULTImpl
import com.kairlec.ultpush.core.handler.EmptyMessageHandler
import com.kairlec.ultpush.core.handler.MessageHandler
import com.kairlec.ultpush.core.pusher.Pusher
import com.kairlec.ultpush.core.pusher.PusherMsg
import com.kairlec.ultpush.wework.message.WeWorkMessage

@ULTImpl("WeWorkMessageHandler", false)
class WeWorkHandler : EmptyMessageHandler<WeWorkMessage>() {
    override val name: String = "[WeWorkHandler]"
    override fun getPusherTypeLiteral(): TypeLiteral<out Pusher<out PusherMsg>> {
        return object : TypeLiteral<Pusher<WeWorkMessage>>() {}
    }

    companion object : TypeStrict {
        override val type = object : TypeLiteral<MessageHandler<WeWorkMessage, WeWorkMessage>>() {}
    }
}