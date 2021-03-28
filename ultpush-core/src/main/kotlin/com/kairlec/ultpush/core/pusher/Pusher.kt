package com.kairlec.ultpush.core.pusher

import com.google.inject.TypeLiteral
import com.kairlec.ultpush.bind.TypeLiteralAble
import com.kairlec.ultpush.bind.ULTInterface
import com.kairlec.ultpush.core.Authenticate
import com.kairlec.ultpush.core.Filter

@ULTInterface(10)
abstract class Pusher<T : PusherMsg> : Authenticate<T>, Filter<T> {
    /**
     * 推送器的名称
     */
    open val name: String = "[Pusher]unnamed@${hashCode()}"

    abstract suspend fun push(msg: PusherMsg)

    override fun allow(content: T) = true

    override fun toString(): String {
        return name
    }
}
